package com.joe.finance.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import com.mongodb.MongoClient;

public class QuoteDao {

	public static final String DB_NAME = "finance";

	private static QuoteDao quoteDao;
	private QuoteCache quoteCache;
	private Datastore datastore;
	private Set<String> symbols;
	private boolean currentLoaded = false;

	private QuoteDao(Datastore datastore) {
		this.datastore = datastore;
	}

	public static QuoteDao quoteDao() {
		if (quoteDao != null) {
			return quoteDao;
		}
		MongoClient mongoClient = new MongoClient(
				"localhost", 27017);
		Datastore ds = new Morphia().createDatastore(mongoClient, DB_NAME);
		quoteDao = new QuoteDao(ds);
		return quoteDao;
	}

	public void insert(List<Quote> quotes) {
		for (Quote quote : quotes) {
			insert(quote);
		}
	}

	public void insert(Quote quote) {
		datastore.save(quote);
	}

	public QuoteCache getCache() {
		if (symbols == null) {
			symbols = new HashSet<>();
		}
		if (quoteCache == null) {
			quoteCache = new QuoteCache(new HashMap<Key, Quote>());
			Query<Quote> query = datastore.createQuery(Quote.class);
			for (Quote quote : query.asList()) {
				symbols.add(quote.getSymbol());
				quoteCache.put(quote.getId(), quote);
			}
		}
		try {
			if (!currentLoaded) {
				quoteCache.loadCurrentQuote(symbols);
				currentLoaded = true;
			}
		} catch (IOException e) {
			System.out.println("Unable to load current quotes...");
		}
		return quoteCache;
	}

}
