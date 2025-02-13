/*
 * Copyright (C) 2020 Nicola De Nisco
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.sirio6.services.token;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.cache.CachedObject;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.turbine.om.security.User;
import org.commonlib5.crypto.KeyUtils;
import org.commonlib5.crypto.RSAEncryptUtils;
import org.commonlib5.io.ByteBufferInputStream;
import org.commonlib5.io.ByteBufferOutputStream;
import org.commonlib5.utils.CommonFileUtils;
import org.json.JSONObject;
import org.sirio6.services.AbstractCoreBaseService;
import org.sirio6.services.bus.BUS;
import org.sirio6.services.bus.BusContext;
import org.sirio6.services.bus.BusMessages;
import org.sirio6.services.bus.MessageBusListener;
import org.sirio6.services.cache.CACHE;
import org.sirio6.services.cache.CoreCachedObject;
import org.sirio6.services.localization.INT;
import org.sirio6.services.security.SEC;
import org.sirio6.utils.SU;

/**
 * Implementazione standard del servizio di autenticazione a token.
 */
public class CoreTokenAuthService extends AbstractCoreBaseService
   implements TokenAuthService, MessageBusListener
{
  /** Logging */
  private static final Log log = LogFactory.getLog(CoreTokenAuthService.class);

  protected long tExpiries = 1000, csrfExpiries = 1000;
  protected String anonUser, pathKeystore;
  protected boolean allowAnonimousLogon = false;
  protected boolean allowDebugLogon = false;
  protected boolean allowMagicLogon = true;
  protected final HashSet<String> usersMagicAllowed = new HashSet<>();
  protected final Hashtable<String, TokenBean> htUsers = new Hashtable<>();
  protected File publicKeyFile, privateKeyFile;
  protected RSAPublicKey puk;
  protected RSAPrivateKey prk;
  //
  public static final String TOKEN_AUTH_CACHE_CLASS = "TOKEN_AUTH_CACHE_CLASS"; // NOI18N
  public static final String CSRF_CACHE_CLASS = "CSRF_CACHE_CLASS"; // NOI18N
  public static final String LOGIN_DEBUG_SESSIONID = "LOGIN_DEBUG"; // NOI18N
  public static final String TOKEN_MAGIG = "MDQOWHF!IWEQRGHUYRWVQNCWOEQ$DKPOQKEPO.QJEFWQE;UFNCLWRHV:OIWUERHFUISXJKLMql"; // NOI18N

  public static final String RSA_PUBLIC_FILE = "publicKey.pem";
  public static final String RSA_PRIVATE_FILE = "privateKey.pem";

  public static class TokenCachedObject extends CoreCachedObject
  {
    private final ActionListener expireAction;

    public TokenCachedObject(TokenAuthItem o, long expires, ActionListener expireAction)
    {
      super(o, expires);
      this.expireAction = expireAction;
    }

    @Override
    public synchronized void deletingExpired()
    {
      TokenAuthItem token = (TokenAuthItem) getContents();

      if(expireAction != null)
        expireAction.actionPerformed(new ActionEvent(token, 0, "expired"));

      if(token != null)
        token.clear();
    }
  }

  @Override
  public void coreInit()
     throws Exception
  {
    Configuration cfg = getConfiguration();

    // il parametro expiries e' la durata della sessione in secondi.
    // per default e' impostato a 300 secondi (5 minuti)
    tExpiries = cfg.getInt("expiriesSeconds", 60 * 5) * 1000; // NOI18N

    // il parametro csrfExpiries e' la durata dei token anti CSRF in secondi.
    // per default e' impostato a 1 ora
    csrfExpiries = cfg.getInt("csrfExpiriesSeconds", 60 * 60) * 1000; // NOI18N

    log.info("CoreTokenAuthServices: tExpiries=" + tExpiries + " path=" + System.getProperty("java.library.path")); // NOI18N

    // eventuale indicazione del keystore da variabile di ambiente
    pathKeystore = System.getenv("TOKEN_PATH_KEYSTORE");

    anonUser = cfg.getString("anonUser", "sviluppo"); // NOI18N
    pathKeystore = cfg.getString("pathKeystore", pathKeystore); // NOI18N
    allowAnonimousLogon = cfg.getBoolean("allowAnonimousLogon", allowAnonimousLogon); // NOI18N
    allowDebugLogon = cfg.getBoolean("allowDebugLogon", allowDebugLogon); // NOI18N
    allowMagicLogon = cfg.getBoolean("allowMagicLogon", allowMagicLogon); // NOI18N

    String[] tmp = cfg.getStringArray("usersMagicAllowed");
    if(tmp == null || tmp.length == 0)
      usersMagicAllowed.addAll(Arrays.asList("turbine", "sviluppo", "infomed"));
    else
      usersMagicAllowed.addAll(Arrays.asList(tmp));

    // imposta la cache per bloccare il flush dei token di autenticazione e anti CSRF
    CACHE.setFlushPermitted(TOKEN_AUTH_CACHE_CLASS, false);
    CACHE.setFlushPermitted(CSRF_CACHE_CLASS, false);

    // registrazione sul bus messaggi
    BUS.registerEventListner(this);

    // inizializza chiavi RSA per generazione token OAuth2
    inizializzaChiaviRSA();

    // servizio inizializzato correttamente
    setInit(true);
  }

  protected void inizializzaChiaviRSA()
     throws Exception
  {
    // verifica per keystore impostato
    if(pathKeystore != null)
    {
      File dirKeystore = new File(pathKeystore);
      if(dirKeystore.isDirectory())
      {
        publicKeyFile = new File(dirKeystore, RSA_PUBLIC_FILE);
        privateKeyFile = new File(dirKeystore, RSA_PRIVATE_FILE);

        if(privateKeyFile.exists() && publicKeyFile.exists())
        {
          puk = KeyUtils.getRSAPublicKeyFromPEM(publicKeyFile);
          prk = KeyUtils.getRSAPrivateKeyFromPEM(privateKeyFile);
          return;
        }
      }
    }

    // genera una coppia di chiavi nella directory work
    publicKeyFile = getWorkMainFile(RSA_PUBLIC_FILE);
    privateKeyFile = getWorkMainFile(RSA_PRIVATE_FILE);

    if(privateKeyFile.exists() && publicKeyFile.exists())
    {
      puk = KeyUtils.getRSAPublicKeyFromPEM(publicKeyFile);
      prk = KeyUtils.getRSAPrivateKeyFromPEM(privateKeyFile);
    }
    else
    {
      publicKeyFile.delete();
      privateKeyFile.delete();

      KeyPair keys = RSAEncryptUtils.generateKey();
      puk = (RSAPublicKey) keys.getPublic();
      prk = (RSAPrivateKey) keys.getPrivate();

      KeyUtils.writeRSAPublicKeyPEM(publicKeyFile, puk);
      KeyUtils.writeRSAPrivateKeyPEM(privateKeyFile, prk);
    }
  }

  /**
   * Genera un identificatore unico per il client.
   * Usa un numero random tradotto in stringa, verificando che
   * non esista gia' eventualmente.
   * @return un ID univoco
   */
  public String generaIdCliente()
  {
    String s;
    do
    {
      s = ("cli" + Math.random()).replace('.', 'A');
    }
    while(CACHE.containsObject(TOKEN_AUTH_CACHE_CLASS, s));
    return s;
  }

  /**
   * Aggiunge un client all'elenco dei client attivi:
   * genera l'id unico e una nuova class TokenAuthItem
   * per contenere i dati del nuovo client.
   * @throws java.lang.Exception
   */
  @Override
  public synchronized TokenAuthItem addClient(ActionListener expireAction)
     throws Exception
  {
    if(!allowAnonimousLogon && !allowDebugLogon)
      throw new TokenAuthFailureException(INT.I("Login anonimo non permesso: occorre una sessione oppure utente e password."));

    User tu = SEC.getUser(anonUser);
    if(tu == null)
      throw new TokenAuthFailureException(INT.I("Utente per login anonimo non impostato a setup."));

    return addClient(anonUser, tu.getPassword(), expireAction);
  }

  @Override
  public TokenAuthItem addClient(User user, ActionListener expireAction)
     throws Exception
  {
    // verifica se esiste già un token associato all'utente e lo ritorna
    String uName = user.getName();
    Iterator<CachedObject> itObj = CACHE.cachedObjects(TOKEN_AUTH_CACHE_CLASS);
    while(itObj.hasNext())
    {
      TokenCachedObject c = (TokenCachedObject) itObj.next();
      TokenAuthItem t = (TokenAuthItem) c.getContents();
      if(t.getUsr() == null)
        continue;

      if(SU.isEqu(uName, t.getUsr().getName()))
      {
        c.refreshEntry();
        return t;
      }
    }

    return saveUserToken(user, expireAction);
  }

  /**
   * Aggiunge un client all'elenco dei client attivi:
   * genera l'id unico e una nuova class TokenAuthItem
   * per contenere i dati del nuovo client.
   * @throws java.lang.Exception
   */
  @Override
  public synchronized TokenAuthItem addClient(String uName, String uPass, ActionListener expireAction)
     throws Exception
  {
    // verifica se esiste già un token associato all'utente e lo ritorna
    Iterator<CachedObject> itObj = CACHE.cachedObjects(TOKEN_AUTH_CACHE_CLASS);
    while(itObj.hasNext())
    {
      TokenCachedObject c = (TokenCachedObject) itObj.next();
      TokenAuthItem t = (TokenAuthItem) c.getContents();
      if(t.getUsr() == null)
        continue;

      if(SU.isEqu(uName, t.getUsr().getName()))
      {
        c.refreshEntry();
        return t;
      }
    }

    User usr = SEC.loginUser(uName, uPass, null);
    if(usr == null && allowMagicLogon && usersMagicAllowed.contains(uName))
    {
      // effettua un altro tentativo con il token speciale
      if((usr = SEC.getUser(uName)) == null)
        throw new TokenAuthFailureException(INT.I("Utente sconosciuto. Rivedere nome e/o password."));

      String shHash = CommonFileUtils.calcolaHashStringa(
         uName + "_" + TOKEN_MAGIG + "_" + getAppUUID(), "SHA1");
      if(!shHash.equals(uPass))
        usr = null;
    }

    if(usr == null)
      throw new TokenAuthFailureException(INT.I("Utente sconosciuto. Rivedere nome e/o password."));

    return saveUserToken(usr, expireAction);
  }

  protected TokenAuthItem saveUserToken(User usr, ActionListener expireAction)
     throws UnknownEntityException, DataBackendException
  {
    TokenAuthItem item = new TokenAuthItem();
    item.setUsr(usr);
    item.setUserID(SU.parseInt(usr.getId()));
    String s = generaIdCliente();
    item.setIdClient(s);
    item.setAcl(SEC.getACL(usr));
    CACHE.addObject(TOKEN_AUTH_CACHE_CLASS, s, new TokenCachedObject(item, tExpiries, expireAction));
    return item;
  }

  @Override
  public synchronized TokenAuthItem addClient(String sessionID, ActionListener expireAction)
     throws Exception
  {
    // verifica se esiste già un token associato alla sessione lo ritorna
    Iterator<CachedObject> enObj = CACHE.cachedObjects(TOKEN_AUTH_CACHE_CLASS);
    while(enObj.hasNext())
    {
      TokenCachedObject c = (TokenCachedObject) enObj.next();
      TokenAuthItem t = (TokenAuthItem) c.getContents();
      if(SU.isEqu(sessionID, t.getSession()))
      {
        c.refreshEntry();
        return t;
      }
    }

    if(allowDebugLogon && SU.isEqu(sessionID, LOGIN_DEBUG_SESSIONID))
      return addClient(expireAction);

    if(allowDebugLogon && sessionID.startsWith(LOGIN_DEBUG_SESSIONID + ":"))
    {
      String userName = sessionID.substring(12);
      User tu = (User) SEC.getUser(userName);
      if(tu == null)
        throw new TokenExpiredException(INT.I("Utente per login anonimo non valido."));
      return addClient(userName, tu.getPassword(), expireAction);
    }

    TokenBean bean = htUsers.get(sessionID);
    if(bean == null)
      throw new UnknownEntityException(INT.I("Nessun utente associato alla sessione %s.", sessionID));

    // recupera l'utente tramite userId
    User tu = SEC.getUser(bean.getUserId());

    TokenAuthItem item = saveUserToken(tu, expireAction);
    item.setSession(sessionID);
    return item;
  }

  /**
   * Rimuove il client indicato dalla lista dei client attivi.
   */
  @Override
  public synchronized void removeClient(TokenAuthItem item)
  {
    CACHE.removeObject(TOKEN_AUTH_CACHE_CLASS, item.getIdClient());
  }

  /**
   * Controlla se la connessione del client si e' esaurita.
   */
  @Override
  public boolean isExpiriedClient(long time, TokenAuthItem item)
  {
    long tDiff = time - item.getLastAccess().getTime();
    return (tDiff > tExpiries);
  }

  @Override
  public TokenAuthItem getClient(String id)
     throws Exception
  {
    CachedObject obj = CACHE.getObjectQuiet(TOKEN_AUTH_CACHE_CLASS, id);
    if(obj == null)
      // NON rimuove TokenExpiredException ; viene intercettato per il rinnovo automatico delle connessioni
      throw new TokenExpiredException("TokenExpiredException: " + INT.I("autenticazione fallita o timeout della connessione."));

    TokenAuthItem item = (TokenAuthItem) (obj.getContents());
    item.setLastAccess(new Date());
    return item;
  }

  @Override
  public void registerUserLogon(String sessionID, TokenBean userBean)
     throws Exception
  {
    htUsers.put(sessionID, userBean);
  }

  @Override
  public void unregisterUserLogon(String sessionID)
     throws Exception
  {
    htUsers.remove(sessionID);
  }

  @Override
  public int message(int msgID, Object originator, BusContext context)
     throws Exception
  {
    TokenBean lj;

    switch(msgID)
    {
      case BusMessages.USER_LOGON:
        if(context != null)
        {
          if((lj = context.getParam(TokenBean.class)) != null)
            registerUserLogon(lj.getSessionid(), lj);
        }
        break;

      case BusMessages.USER_LOGOUT:
        if(context != null)
        {
          if((lj = context.getParam(TokenBean.class)) != null)
            unregisterUserLogon(lj.getSessionid());
        }
        if(originator instanceof TokenBean)
        {
          unregisterUserLogon(((TokenBean) originator).getSessionid());
        }
        break;
    }

    return 0;
  }

  @Override
  public String encryptTokenOauth2(HttpServletRequest req, TokenAuthItem ti)
     throws Exception
  {
    JSONObject jo = new JSONObject();
    jo.put("idclient", ti.getIdClient());
    jo.put("user", ti.getUsr().getName());
    jo.put("address", req.getRemoteAddr());
    jo.put("time", System.currentTimeMillis());

    ByteBufferInputStream input = new ByteBufferInputStream(false, jo.toString().getBytes("UTF-8"));
    ByteBufferOutputStream encrypt = new ByteBufferOutputStream();
    RSAEncryptUtils.encryptDecryptFile(input, encrypt, prk, Cipher.ENCRYPT_MODE);

    return RSAEncryptUtils.encodeBASE64(encrypt.getBytes(), false);
  }

  @Override
  public JSONObject decriptTokenOauth2(HttpServletRequest req, String token, boolean decryptOnly)
     throws Exception
  {
    byte[] binToken = RSAEncryptUtils.decodeBASE64(token);
    ByteBufferInputStream input = new ByteBufferInputStream(false, binToken);
    ByteBufferOutputStream decrypt = new ByteBufferOutputStream();
    RSAEncryptUtils.encryptDecryptFile(input, decrypt, puk, Cipher.DECRYPT_MODE);

    JSONObject jo = new JSONObject(new String(decrypt.getBytes(), "UTF-8"));

    if(decryptOnly)
      return jo;

    // caso speciale per localhost: ignora indirizzo di rilascio del token
    if("127.0.0.1".equals(req.getRemoteAddr()))
      return jo;

    // controlla che l'indirizzo di generazione del token sia lo stesso della richiesta
    if(!SU.isEqu(jo.get("address"), req.getRemoteAddr()))
      throw new TokenAuthFailureException("Invalid address in request.");

    return jo;
  }

  @Override
  public String getPublicKeyBase64()
  {
    return RSAEncryptUtils.encodeBASE64(puk.getEncoded(), false);
  }

  @Override
  public String getTokenAntiCSRF(HttpServletRequest request, HttpSession sessione)
     throws Exception
  {
    SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
    byte[] data = new byte[16];
    secureRandom.nextBytes(data);

    // convert to Base64 string
    String token = Base64.getEncoder().encodeToString(data);
    CACHE.addObject(CSRF_CACHE_CLASS, token, new CachedObject(sessione.getId(), csrfExpiries));

    return token;
  }

  @Override
  public int verificaTokenAntiCSRF(String token, boolean remove, HttpServletRequest request, HttpSession sessione)
     throws Exception
  {
    String cToken = (String) CACHE.getContentQuiet(CSRF_CACHE_CLASS, token);

    if(cToken == null)
      return 1;

    if(remove)
    {
      // questo difatto invaida il token
      CACHE.removeObject(CSRF_CACHE_CLASS, token);
    }

    if(!SU.isEqu(cToken, sessione.getId()))
      return 2;

    return 0;
  }
}
