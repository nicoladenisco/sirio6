/////////////////////////////////////////////////
// finestre di pop up

// apre una finestra cambiando la posizione e le dimensioni
// scalandole opportunamente considerando quelle originali
// riferite ad uno schermo 1024x768 pixels
function openRelativeTo1024x768Window(urlwin, titolo, posx, posy, dimx, dimy) {
  var px = (posx * screen.width) / 1024;
  var py = (posy * screen.height) / 768;
  var dx = (dimx * screen.width) / 1024;
  var dy = (dimy * screen.height) / 768;

  titolo = titolo.toString().replace(' ', '_');
  secureOpenWindow(urlwin, titolo,
          "menubar=no,personalbar=no,status=no,titlebar=no," +
          "screenX=" + px + ",screenY=" + py +
          ",top=" + py + ",left=" + px + ",width=" + dx + ",height=" + dy +
          ",resizable=yes,alwaysRaised=yes,scrollbars=yes");
}

function openWindow(urlwin, titolo, px, py, dx, dy) {
  titolo = titolo.toString().replace(' ', '_');
  secureOpenWindow(urlwin, titolo,
          "menubar=no,personalbar=no,status=no,titlebar=no," +
          "screenX=" + px + ",screenY=" + py +
          ",top=" + py + ",left=" + px + ",width=" + dx + ",height=" + dy +
          ",resizable=yes,alwaysRaised=yes,scrollbars=yes");
}

function secureOpenWindow(urlwin, titolo, params) {
  var jspUri = jsContextPath + "/json/hideParams.jsp";
  rigel.syncJSON(jspUri + "?origin=" + encodeURIComponent(urlwin), function (data) {
    var mywin = window.open(data.purgedUri, titolo, params);
    mywin.focus();
  });
}

// finestra di popup standard
function openStandardWindow(urlwin, titolo) {
  openRelativeTo1024x768Window(urlwin, titolo, 300, 120, 800, 550);
}

// finestra ricerca articoli ec
function openArticoliWindow(urlwin, titolo) {
  openRelativeTo1024x768Window(urlwin, titolo, 300, 120, 700, 500);
}

// finestra di popup dettaglio
function openDettaglioWindow(urlwin, titolo) {
  openRelativeTo1024x768Window(urlwin, titolo, 200, 220, 700, 400);
}

// finestra di popup informazioni
function openInfoWindow(urlwin, titolo) {
  openRelativeTo1024x768Window(urlwin, titolo, 100, 320, 700, 400);
}

// finestra di edit informazioni
function openEditWindow(urlwin, titolo) {
  openRelativeTo1024x768Window(urlwin, titolo, 50, 50, 900, 650);
}

// finestra di edit informazioni
function openMaximizedWindow(urlwin, titolo) {
  if (navigator.userAgent.match(/Firefox/g)) {
    titolo = titolo.toString().replace(' ', '_');
    secureOpenWindow(urlwin, titolo,
            "menubar=no,personalbar=no,status=no,titlebar=no,fullscreen=yes," +
            ",resizable=no,alwaysRaised=yes,scrollbars=yes");
  }
  else {
    openRelativeTo1024x768Window(urlwin, titolo, 0, 0, 1024, 768);
    maximizeWindow();
    window.focus();
  }
}

// finestra di edit informazioni
function openAliceWindow(urlwin, titolo) {
  openRelativeTo1024x768Window(urlwin, titolo, 50, 50, 500, 600);
}

// finestra di edit informazioni
function openPrintWindow(urlwin, titolo) {
  openRelativeTo1024x768Window(urlwin, titolo, 50, 50, 900, 650);
}

// finestra di edit informazioni
function openLauncherWindow(urlwin, titolo) {
  openRelativeTo1024x768Window(urlwin, titolo, 50, 50, 300, 50);
}

function maximizeWindow() {
  window.moveTo(0, 0);
  window.resizeTo(screen.width, screen.height);
}

function relCommandJump(url) {
  eval("location='" + url + "'");
}

function goPage(src) {

  var jspUri = jsContextPath + "/json/hideParams.jsp";
  rigel.syncJSON(jspUri + "?origin=" + encodeURIComponent(src), function (data) {
    window.location.href = data.purgedUri;
  });

}

function goLink(src) {
  try
  {
    showLoading();
  }
  catch (e)
  {
  }
  goPage(src);
}

function openInNewTab(url) {
  var win = window.open(url, '_blank');
  win.focus();
}

///////////////////////////////////////////////
// funzioni per form

function convertiCampoMaiuscolo(campo) {
  campo.value = campo.value.toUpperCase();
}

function convertiCampoMinuscolo(campo) {
  campo.value = campo.value.toLowerCase();
}

function ControllaCF(cf) {
  var validi, i, s, set1, set2, setpari, setdisp;
  if (cf == '')
    return '';

  cf = cf.toUpperCase();
  if (cf.length != 16)
    return "La lunghezza del codice fiscale non e'\n" +
            "corretta: il codice fiscale deve essere lungo\n" +
            "esattamente 16 caratteri.\n";

  validi = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  for (i = 0; i < 16; i++)
  {
    if (validi.indexOf(cf.charAt(i)) == -1)
      return "Il codice fiscale contiene un carattere non valido `" +
              cf.charAt(i) +
              "'.\nI caratteri validi sono le lettere e le cifre.\n";
  }
  set1 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  set2 = "ABCDEFGHIJABCDEFGHIJKLMNOPQRSTUVWXYZ";
  setpari = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  setdisp = "BAKPLCQDREVOSFTGUHMINJWZYX";
  s = 0;
  for (i = 1; i <= 13; i += 2)
    s += setpari.indexOf(set2.charAt(set1.indexOf(cf.charAt(i))));
  for (i = 0; i <= 14; i += 2)
    s += setdisp.indexOf(set2.charAt(set1.indexOf(cf.charAt(i))));
  if (s % 26 != cf.charCodeAt(15) - 'A'.charCodeAt(0))
    return "Il codice fiscale non e' corretto:\n" +
            "il codice di controllo non corrisponde.\n";
  return "";
}

function ControllaPIVA(pi) {
  if (pi == '')
    return '';
  if (pi.length != 11)
    return "La lunghezza della partita IVA non e'\n" +
            "corretta: la partita IVA deve essere lunga\n" +
            "esattamente 11 caratteri.\n";
  validi = "0123456789";
  for (i = 0; i < 11; i++)
  {
    if (validi.indexOf(pi.charAt(i)) == -1)
      return "La partita IVA contiene un carattere non valido `" +
              pi.charAt(i) + "'.\nI caratteri validi sono le cifre.\n";
  }
  s = 0;
  for (i = 0; i <= 9; i += 2)
    s += pi.charCodeAt(i) - '0'.charCodeAt(0);
  for (i = 1; i <= 9; i += 2)
  {
    c = 2 * (pi.charCodeAt(i) - '0'.charCodeAt(0));
    if (c > 9)
      c = c - 9;
    s += c;
  }
  if ((10 - s % 10) % 10 != pi.charCodeAt(10) - '0'.charCodeAt(0))
    return "La partita IVA non e' valida:\n" +
            "il codice di controllo non corrisponde.\n";
  return '';
}

// ritorna vero se la stringa e vuota, nulla o
// contiene solo spazi bianchi
function isValNull(val) {
  if (val == null || val == "")
    return true;

  // cerca un carattere qualsiasi deverso da spazio
  for (var i = 0; i < val.length; i++)
  {
    if (val.charAt(i) != ' ')
      return false;
  }

  return true;
}

function testCampoNull(campo, nome) {
  if (!isOkStr(campo.value)) {
    stdfuncError("Il campo " + nome + " non puo' essere vuoto!");
    campo.focus();
    return false;
  }
  return true;
}

function testCampoZero(campo, nome) {
  if (!isOkStr(campo.value) || parseInt(campo.value) == 0) {
    stdfuncError("Il campo " + nome + " non contiene un valore valido!");
    campo.focus();
    return false;
  }
  return true;
}

function testCampoAlfanumerico(campo, nome) {
  return true;
}

function testCampoNumericoIntero(campo, nome) {
  var val = trim(campo.value);
  if (val.match(/^[\+\-]*[0-9]+$/)) {
    return true;
  }

  stdfuncError("Il campo " + nome + " puo' contenere solo i numeri da 0 a 9 ed eventualmente preceduti da + o -");
  campo.focus();
  return false;
}

function testCampoNumericoFloat(campo, nome) {
  var val = trim(campo.value);
  if (val.match(/^[\+\-]*[0-9\.\,e]+$/)) {
    return true;
  }

  stdfuncError("Il campo " + nome + " puo' contenere solo i numeri da 0 a 9 eventualmente preceduti da + o - e la virgola");
  campo.focus();
  return false;
}

function testCampoRangeIntero(campo, nome, valmin, valmax) {
  var val = parseInt(campo.value);
  if (val >= valmin && val <= valmax)
    return true;

  stdfuncError("Il campo " + nome + " non contiene un valore valido!\nDeve essere compreso fra " + valmin + " e " + valmax);
  campo.focus();
  return false;
}

function testCampoRangeFloat(campo, nome, valmin, valmax) {
  var val = parseFloat(campo.value);
  if (val >= valmin && val <= valmax)
    return true;

  stdfuncError("Il campo " + nome + " non contiene un valore valido!\nDeve essere compreso fra " + valmin + " e " + valmax);
  campo.focus();
  return false;
}

function testCampoTelefono(campo, nome) {
  var val = trim(campo.value);
  if (val.match(/^[\+]*[0-9\.]+$/)) {
    return true;
  }

  stdfuncError("Il campo " + nome + " puo' contenere solo i numeri da 0 a 9 ed eventualmente + e . (ES: +39.081.234567)");
  campo.focus();
  return false;
}

function testCampoData(campo, nome) {
  var val = campo.value;

  // una stringa vuota è OK; eventualmente utilizzare testNull()
  if (!isOkStr(val))
    return true;

  // il parsing delle date è molto sofisticato e non può essere
  // controllato dal lato client: occorre comunque fare la post del form
  return true;
}

function testCampoEMail(campo, nome) {
  var val = trim(campo.value);
  var re = /^[\w\.-]+@[\w\.-]+\.[a-z]{2,4}$/;

  // un valore NULL viene considerato valido;
  // si puo' eventualmente usare un testnull
  if (isValNull(val))
    return true;

  var valid = re.test(val);
  if (!valid) {
    stdfuncError("Il campo " + nome + " è compilato in modo errato.");
    campo.focus();
    return false;
  }

  return true;
}

function testCampoCodice(campo, nome) {
  var val = trim(campo.value);
  var re = /^[a-z|A-Z|0-9|_|-]*$/;

  // un valore NULL viene considerato valido;
  // si puo' eventualmente usare un testnull
  if (isValNull(val))
    return true;

  var valid = re.test(val);
  if (!valid) {
    stdfuncError("Il campo " + nome + " non contiene un codice valido.\n" +
            "Sono consentiti solo caratteri alfanumerici senza spazi.");
    campo.focus();
    return false;
  }

  return true;
}

function testCampoCodFis(campo, nome) {
  var val = trim(campo.value);

  // un valore NULL viene considerato valido;
  // si puo' eventualmente usare un testnull
  if (isValNull(val))
    return true;

  if (val == "")
    return true;

  if (val.length == 11)
    return testCampoPIVA(campo, nome);

  var err = ControllaCF(val);

  if (err > '') {
    stdfuncError("VALORE ERRATO nel campo " + nome + "\n\n" + err + "\nCorreggi e riprova!");
    campo.focus();
    return false;
  }

  return true;
}

function testCampoPIVA(campo, nome) {
  var val = trim(campo.value);

  // un valore NULL viene considerato valido;
  // si puo' eventualmente usare un testnull
  if (isValNull(val))
    return true;

  if (val == "")
    return true;

  var err = ControllaPIVA(val);

  if (err > '') {
    stdfuncError("VALORE ERRATO nel campo " + nome + "\n\n" + err + "\nCorreggi e riprova!");
    campo.focus();
    return false;
  }

  return true;
}

function impostaIdText(campo, testo) {
  document.getElementById(campo).childNodes[0].nodeValue = testo;
}

function notImplemented() {
  stdfuncError("Spiacente. La funzione richiesta non e' ancora disponibile.");
}

function toggleCheck(thisField) {
  checkSet = eval("document.fo." + thisField);
  checkSet.checked = !(checkSet.checked);
}

function toggleRadio(thisField, thisValue) {
  radioSet = eval("document.fo." + thisField);

  for (i = 0; i < radioSet.length; i++)
  {
    if (radioSet[i].value == thisValue)
      radioSet[i].checked = true;
  }
}

function setValueSafe(thisField, thisValue) {
  try
  {
    control = eval("document.fo." + thisField);
    control.value = thisValue;
  }
  catch (e)
  {
  }
}

function apriFinestraListaFiltrata(url, filtro, tipo) {
  idx_ = filtro.indexOf('*');

  filterField = filtro.substring(0, idx_)

  filterFieldTarget = filtro.substring(idx_ + 1, filtro.length)

  VL_fieldName = 'VL' + filterFieldTarget
  elements = document.getElementsByName(filterField)

  VL_fieldValue = elements[0].value
  OP_fieldName = 'OP' + filterFieldTarget
  // provvisoriamente cablato a 7
  OP_fieldValue = 1

  filterdUrl = url + '&' + VL_fieldName + '=' + VL_fieldValue + '&' + OP_fieldName + '=' + OP_fieldValue
  apriFinestraLista(filterdUrl, tipo)
}

function leftTrim(stringa) {
  while (stringa.substring(0, 1) == ' ')
  {
    stringa = stringa.substring(1, stringa.length);
  }
  return stringa;
}

function rightTrim(stringa) {
  while (stringa.substring(stringa.length - 1, stringa.length) == ' ')
  {
    stringa = stringa.substring(0, stringa.length - 1);
  }
  return stringa;
}

function trim(stringa) {
  while (stringa.substring(0, 1) == ' ')
  {
    stringa = stringa.substring(1, stringa.length);
  }
  while (stringa.substring(stringa.length - 1, stringa.length) == ' ')
  {
    stringa = stringa.substring(0, stringa.length - 1);
  }
  return stringa;
}

function isOkStr(stringa) {
  if (stringa == null || stringa == "")
    return false;

  for (var i = 0; i < stringa.length; i++)
  {
    if (stringa.charAt(i) != ' ')
      return true;
  }

  return false;
}

function isFilled(field, fieldName) {
  if (field.value == "") {
    stdfuncError("Valorizzare il campo " + fieldName)
    return false;
  }
  return true;
}

function isValidTime(timeField, timeFieldName) {
  timeValue = timeField.value;
  if (timeValue.match(/^(20|21|22|23|[01]\d|\d)(([:][0-5]\d){1,2})$/)) {
    return true;
  }
  else {
    stdfuncError('Inserire in formato HH:mm la ora nel campo ' + timeFieldName);
    return false;
  }
}

function isValidDate(dateField, dateFieldName) {
  dateValue = dateField.value;
  if (dateValue.match(/^(0[1-9]|[12][0-9]|3[01])[- \/.](0[1-9]|1[012])[- \/.](19|20)[0-9]{2}$/)) {
    return true;
  }
  else {
    stdfuncError('Inserire in formato dd/MM/yyyy la data nel campo ' + dateFieldName);
    return false;
  }
}

function isNumeric(numericField, numericFieldName) {
  numericValue = trim(numericField.value);
  if (numericValue.match(/^\d+$/)) {
    return true;
  }

  stdfuncError('Nel campo ' + numericFieldName + ' sono ammessi solo caratteri numerici');
  return false;
}

function isValidDateRange(startDateField, endDateField, startDateFieldName, endDateFieldName) {
  var data1 = trim(startDateField.value);
  var data2 = trim(endDateField.value);
  if (data1 == "" && data2 != "")
    return false;
  if (data1 != "" && data2 == "")
    return true;

  var oggetto1 = new Date(parseInt(data1.substr(6)), parseInt(data1.substr(3, 2), 10), parseInt(data1.substr(0, 2)));
  var oggetto2 = new Date(parseInt(data2.substr(6)), parseInt(data2.substr(3, 2), 10), parseInt(data2.substr(0, 2)));
  if (oggetto2 - oggetto1 < 0) {
    stdfuncError("Attenzione. il valore del campo " + endDateFieldName + " è antecedente quello del campo " + startDateFieldName + ".");
    endDateField.focus();
    return false;
  }
  return true;
}

function isValidTimeRange(startTimeField, endTimeField, startTimeFieldName, endTimeFieldName) {
  var time1 = trim(startTimeField.value);
  var time2 = trim(endTimeField.value);
  if (time1 == "" && time2 != "")
    return false;
  if (time1 != "" && time2 == "")
    return true;

  ora1 = time1.substr(0, 2);
  min1 = time1.substr(3, 2);
  minHours1 = (ora1 * 60) + min1;
  ora2 = time2.substr(0, 2);
  min2 = time2.substr(3, 2);
  minHours2 = (ora2 * 60) + min2;
  if (minHours2 - minHours1 < 0) {
    stdfuncError("Attenzione. il valore del campo " + endTimeFieldName + " è antecedente quello del campo " + startTimeFieldName + ".");
    endTimeField.focus();
    return false;
  }
  return true;
}

function updateDiv(idDiv, str) {
  document.getElementById(idDiv).innerHTML = str;
}

function confermaCB(prompt, url) {
  // DEFINITA IN CommonHead.vm (dipende dal tookit utilizzato)
  stdfuncPrompt(prompt, url);
}

function sleep(millis) {
  var date = new Date();
  var curDate = null;
  do
  {
    curDate = new Date();
  }
  while (curDate - date < millis);
}

/**
 * Funzione per la gestione dei tasti frecce.
 * Consente di navigare in un form. Ad ogni campo di input si lega
 * all'evento keydown indicando il campo precedente (freccia in alto)
 * e il campo successivo (freccia in basso).
 * <code>
 *  ...  onkeydown="return moveKey(document.fo.VL_fo_fun, document.fo.VL_fo_snome, event);" ...
 * <code>
 * @param {type} campoup campo precedente
 * @param {type} campodown campo successivo
 * @param {type} e descrittore evento
 * @returns {Boolean} vero se tasto non gestito
 */
function moveKey(campoup, campodown, e) {
  switch (getKeyCodeFromEvent(e))
  {
    case 37: // freccia sx
    case 39: // freccia dx
      break;

    case 38: // freccia su
      campoup.focus();
      campoup.select();
      return false;

    case 40: // freccia giu
      campodown.focus();
      campodown.select();
      return false;
  }

  return true;
}

/**
 * Funzione portatile per determinare il tasto premuto di un evento.
 * Testata su Firefox e Chrome.
 * @param {type} e evento
 * @returns key code
 */
function getKeyCodeFromEvent(e) {
  if (e === null)
    e = event;

  var eventChooser = e.which;
  if (eventChooser === 0)
    eventChooser = e.keyCode;

  return eventChooser;
}

// SPOSTATA IN CommonHead.vm (dipende dal tookit utilizzato)
//function stdfuncError(msg) {
//  jError(msg);
//}

/**
 * Formattazione di stringhe con parametri.
 * Supporta le sintassi:
 * formatString("i can speak {language} since i was {age}",{language:'javascript',age:10});
 * formatString("i can speak {0} since i was {1}",'javascript',10}); *
 * @param {type} str stringa da formattare
 * @param {type} col token per la formattazione
 * @returns stringa formattata
 */
function formatString(str, col) {
  col = typeof col === 'object' ? col : Array.prototype.slice.call(arguments, 1);

  return str.replace(/\{\{|\}\}|\{(\w+)\}/g, function (m, n) {
    if (m === "{{") {
      return "{";
    }
    if (m === "}}") {
      return "}";
    }
    return col[n];
  });
}

/**
 * Esegue una richiesta json in modalità sincrona.
 * A differenza di jQuery.getJSON() questa è sincrona
 * ovvero aspetta la risposta ed esegue la callback prima di ritornare.
 * @param {type} uri
 * @param {type} callbackDaChiamare
 * @returns {undefined}
 */
function syncJSON(uri, callbackDaChiamare) {
  jQuery.ajax({
    dataType: "json",
    url: uri,
    async: false,
    success: function (data) {
      callbackDaChiamare(data);
    }
  });
}

/**
 * Funzione modulo pura.
 * In javascript l'operatore modulo non è lo stesso
 * degli altri linguaggi. Questa funzione produce l'effetto
 * dell'operatore modulo puro.
 * @param {type} a
 * @param {type} n
 * @returns il resto della divisione di a per n.
 */
function modulo(a, n) {
  return ((a % n) + n) % n;
}

/**
 * Funzione per la gestione dei tasti frecce.
 * Consente di navigare in un form. Ad ogni campo di input si lega
 * all'evento keydown indicando il campo precedente (freccia in alto)
 * e il campo successivo (freccia in basso).
 * Va aggiunta come handlre dell'evento keydown al campo di interesse.
 * Riceve un array di controlli fra cui ciclare; se premiamo freccia
 * identifica il campo corrente nell'array e si sposta al campo precedente
 * o successivo dello stesso array.
 * Vedi moveKeyArrayInitializer.
 *
 * @param {type} arInputs array di controlli fra cui ciclare
 * @param {type} event evento di tipo keydown
 * @returns {Boolean} vero se tasto non gestito
 */
function moveKeyArray(arInputs, event) {
  switch (getKeyCodeFromEvent(event))
  {
    case 38: // freccia su
    case 40: // freccia giu
      break;

    default:
      return true;
  }

  for (var i = 0; i < arInputs.length; i++)
  {
    if (event.srcElement === arInputs[i]) {
      var n = modulo(i + 1, arInputs.length);
      var p = modulo(i - 1, arInputs.length);

      var pr = arInputs[p];
      var ne = arInputs[n];
      if (moveKey(pr, ne, event))
        return true;
      event.preventDefault();
      return false;
    }
  }

  return true;
}

/**
 * Inizializza un pacchetto di controlli input per la navigazione
 * con frecciasu/frecciagiu.
 * Ad ogni controllo aggancia un opportuno gestore del keydown
 * per consentire la navigazione
 * @param {type} arInputs array di controlli fra cui ciclare
 */
function moveKeyArrayInitializer(arInputs) {
  for (var i = 0; i < arInputs.length; i++)
  {
    arInputs[i].addEventListener("keydown", function (event) {
      moveKeyArray(arInputs, event);
    });
  }
}

/**
 * Come moveKeyArrayInitializer ma usa stringa come selettore JQuery
 * per determinare il pacchetto di controlli su cui ciclare.
 * @param {string} selector selettore JQuery
 */
function moveKeyArrayInitializerJQuery(selector) {
  var arInputs = $(selector);
  moveKeyArrayInitializer(arInputs);
}

/**
 * Seleziona oggetti DOM usando un selettore XPath.
 * @param {type} STR_XPATH una xpath del tipo /html/body/div/div/div[1]/div[1]/div/div[3]/div[1]
 * @returns {Array|_x.xnodes} array di nodi che corrispondono alla path
 */
function _x(STR_XPATH) {
  var xresult = document.evaluate(STR_XPATH, document, null, XPathResult.ANY_TYPE, null);
  var xnodes = [];
  var xres;
  while (xres = xresult.iterateNext())
  {
    xnodes.push(xres);
  }

  return xnodes;
}

/**
 * Copia il testo specificato nella clipboard.
 * @param {type} text testo da copiare
 * @returns {undefined}
 */
function copyToClipboard(text) {
  var $temp = $("<input>");
  $("body").append($temp);
  $temp.val(text).select();
  document.execCommand("copy");
  $temp.remove();
}

/**
 * Come copyToClipboard() ma con visualizzazione di un alert di notifica all'utente.
 * @param {type} text testo da copiare
 * @returns {undefined}
 */
function copyToClipboardAlert(text) {
  copyToClipboard(text);
  alert("Messaggio copiato nella clipboard.");
}
