package jacusa.filter.storage;

public class DummyFilterFillCache extends AbstractFilterStorage<Void> {
	
	public DummyFilterFillCache(final char c) {
		super(c, 0);
	}

	@Override
	public void clearContainer() {}

}