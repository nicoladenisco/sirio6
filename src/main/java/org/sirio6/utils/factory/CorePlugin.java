package org.sirio6.utils.factory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca una classe come implementazione di un plugin Sirio.
 * <br>
 * esempio:
 *
 * <pre>
 * @CorePlugin(name = "named-plugin", properties = {
 * "driver=org.postgresql.Driver",
 * "mapping=first",
 * "mapping=second"
 * })
 * ...
 * ...
 * CoreAbstractPluginFactory[InterfacciaPlugin] factory = ...
 * InterfacciaPlugin plugin = factory.getPlugin("named-plugin");
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CorePlugin
{
  /**
   * Nome del plugin.
   * Per default è il nome della classe.
   * @return nome di istanza da usare in getPlugin()
   */
  String name() default "";

  /**
   * Proprietà nel formato utilizzato dai file .properties, una per elemento.
   * Per default nessuna proprieta.
   * ogni stringa ha la forma chiave=valore
   * @return array di stringhe
   */
  String[] properties() default
  {
  };
}
