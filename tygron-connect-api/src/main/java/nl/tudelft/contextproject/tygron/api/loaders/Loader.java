package nl.tudelft.contextproject.tygron.api.loaders;

import nl.tudelft.contextproject.tygron.api.Session;

public abstract class Loader<T> {
  private T cached;

  Session session;

  public Loader(Session session) {
    this.session = session;
  }
  
  public Loader() {
  }  

  protected abstract T load();

  public T reload() {
    cached = load();
    return cached;
  }

  public T get() {
    if (cached == null) {
      cached = load();
    }
    return cached;
  }

  public abstract Class<T> getDataClass();

  public abstract RefreshInterval getRefreshInterval();

  public enum RefreshInterval {
    NEVER, NORMAL
  }
}