
package org.jruby.test;

public class BetaSingleton {

  public static final BetaSingleton getInstance() {
    return INSTANCE;
  }
  private static final BetaSingleton INSTANCE = new BetaSingleton();
  private BetaSingleton() { /* no-op */ }

  public String getBeta() {
      return "Beta";
  }

  public String beta() {
      return "beta";
  }

  public boolean isBeta() {
      return true;
  }

  // class :

  public static String betaClass() {
      return "betaClass";
  }

  public static boolean isBetaClass() {
      return true;
  }

  public static String getBetaClass() {
      return "BetaClass";
  }

  // 2

  public static boolean isBetaClass2() {
    return true;
  }

  public static String getBetaClass2() {
    return "BetaClass2";
  }

  // 3

  public static String getBetaClass3() {
    return "BetaClass3";
  }

  public static boolean isBetaClass3() {
    return true;
  }

  // 4

  public static String getBetaClass4() {
    return "BetaClass4";
  }

  public static Object betaClass4() {
    return "betaClass4";
  }

  // 2

  public String getBeta2() {
    return "Beta2";
  }

  public boolean isBeta2() {
    return true;
  }

  // 3

  public boolean isBeta3() {
    return true;
  }

  public String getBeta3() {
    return "Beta3";
  }

  // 4

  public boolean beta4() {
    return true;
  }

  public String getBeta4() {
    return "Beta4";
  }

  // 5

  public String getBeta5() {
    return "Beta5";
  }

  public boolean beta5(final Object arg) {
    return true;
  }

  // 6

  public Object beta6() {
    return "beta6";
  }

  public boolean isBeta6() {
    return true;
  }

  // 7

  public boolean isBeta7() {
    return true;
  }

  public Boolean beta7() {
    return null;
  }

}
