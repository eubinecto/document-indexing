# Coursework : Document Indexing in MapReduce
- author: Eu-Bin KIM (10327741)
- date: 24th of November 2020
- word count : 834 (assume 100 less words)

## Functionality
### Case folding
Normalising case of all terms to lowercase is implemented in line 85 of the code. Some words are in uppercase 
only because they are used to starting a sentence (e.g. "In"  as in "In the episode, ...") , and setting all of them to lowercase 
normalises them to match with those used in the middle of a sentence. (e.g. "in" in "... partially in response to"). One evident 
disadvantage of this is that we can no longer disambiguate Proper Nouns from Nouns (e.g. "apple(fruit)" from "Apple(company")). 
Yet, it should be noted that such drawbacks could be compensated with the context provided in a query (e.g. "eat apple", "apple's stock price").

### Stop-words filtering
Stop-words filtering is implemented in line 86 and 161 of the code. Stop-words are words that occur in abundance
 such as "my", "is" and "the". In the code, they are filtered out in the tokenisation process as
  they may hold (Nenadic, 2020) low semantic significance. While this might be helpful for denoising search query,
   there is a domain-specific downside to this. When people search for a particular episode, one might search for a 
   memorable quote from the episode, say, "I did mean it". However, the three terms would not be indexed
    as all of them are listed as Stop-words in `lib/stopwords.txt`. Although none of the Simpson episode wiki's
     include the quote, but if they did, searching the quote would return nothing. 
    
 
### Stemming
  - how? do you stem all the words? or do conditional stemming?
  - pros: ??
  - cons: ??


### Domain-specific filtering
Filtering out date, url, ISBN, numbers and page numbers is implemented in line 79-83 of the code. Wikipedia pages include
 in-line citations and "References" section, both of which are not worthy of indexing simply because rarely would people use
 them to search for an episode. They might be worthy if we were building an index for a book search engine however, as people
might want to find books by an ISBN number, or jump into a specific page in a book. This well illustrates that tokenisation process
 depends (Nenadic, 2020) on the domain of the data. 


### Document Frequency (DF) and Term Frequency (TF)

> ```
> allow|2	[Bart_the_Fink.txt.gz|3|[423, 548, 603], ...]
> alter|2	[Bart_the_Fink.txt.gz|2|[782, 967], ...]
> air|6	[Bart_the_Fink.txt.gz|3|[147, 205, 220]
> ```
> *Figure 2* : Parts of the Inverted Index for the terms `allow` , `alter` and `american`.
>  DF (next to the term), TF (next to the document) and positional indices (next to TF) are stored. 

*Figure 3* : The formula for TF-IDF weight of a term (Nenadic, 2020). | 
--- |
![](.report_images/c2855e44.png) | 

 Computation of TF and DF is implemented in line 281 and 320 of the code, respectively. They are necessary to compute TF-IDF weights
  for each term according to the formula in *Figure 3*. TF-IDF weights help us measure the significance of the terms 
  in relation to the documents they appear. Take the index shown in `Figure 2` for example; `air` has higher DF than
   the other two terms while its TF being nearly identical to that of the others.
    Since TF-IDF is inversely proportional to the value of DF (DF is a denominator of the input to a monotonically increasing logarithm),
     we can see that `air` holds less significance in `Bart_the_Fink.txt.gz` than `allow` or `alter` does. 
  

### Positional Indexing

Positional Indexing is implemented in line 205-221 of the code, which utilises `Counter` to keep 
track of term positions. A Positional Index specifies (Christopher D. Manning, et.al, 2008) the positions at which
 the term appears in the postings. For example, *Figure 2* above shows the positional index for `allow`; 
 the term appears at 423rd, 548th and 603rd positions in the document `Bart_the_Fink.txt.gz`.
   Explicitly specifying term positions is useful for an efficient proximity search.
   That is, if we were to search for "allow to alter" on the index(*Figure 2*),
we can efficiently work out that the two terms appear closer in `Bart_the_Fink.txt.gz` than in
 `Bart_the_Lover.txt.gz` (782 - 603 = 179 < 581 - 329 = 252), and that the former should be more relevant to the query than
 the latter.
 
 
## Performance


> in-mapper aggregation | time (seconds)
> --- | --- 
> NO | 21.782
> YES | 22.549
>
> *Figure 4* : The time it took to run jobs, with and without In-mapper Aggregation


In-mapper Aggregation pattern, where aggregation of values takes place (Paton et.al, 2020) in `map`, is used in the code.
 As *Figure 4* illustrates, adopting the pattern has led to an increase in the performance. This is because 
 aggregating intermediate TF's and Positional Index's inside the mapper alleviated the bottleneck caused by
  fewer reducers being available than they are ideally needed. However, it should be noted that this would not have been 
  the case if there existed numerous duplicated terms in each file split; In an extreme case where all file splits
  contains duplicated terms only, the mapper would iterate over `tokens` 2 times (first for aggregation, then for emitting)
   only to produce the same key-value pairs as what simple mapper would produce by iterating `tokens` only once. In such 
   a case, mappers with In-mapper Aggregation pattern would in fact take twice more time than simple mappers would.



## References
- (Christopher D. Manning, et.al, 2008) , *Introduction to Information Retrieval*
  - entry: https://nlp.stanford.edu/IR-book/html/htmledition/irbook.html
  - positional indexing: https://nlp.stanford.edu/IR-book/html/htmledition/positional-indexes-1.html
- Nenadic, Goran. 2020 *Preparing to Index*
  - link: https://online.manchester.ac.uk/bbcswebdav/pid-11983753-dt-content-rid-51278176_1/courses/I3132-COMP-38211-1201-1SE-039876/Workshop-02-01%283%29.pdf
- Nenadic, Goran. 2020, *Querying and ranking: Ranked retrieval*
  - link: https://online.manchester.ac.uk/bbcswebdav/pid-12081152-dt-content-rid-55239820_1/courses/I3132-COMP-38211-1201-1SE-039876/Workshop-04-02.pdf
- Nenadic, Goran. 2020 *Principles of IR – Indexing*
  - link:  https://online.manchester.ac.uk/bbcswebdav/pid-11983725-dt-content-rid-50339139_1/courses/I3132-COMP-38211-1201-1SE-039876/Workshop-01_04_inverted_index%281%29.pdf
- Paton, Norman. Batista-Navarro, Riza. Sampaio, Sandra. 2020, *Map Reduce Design Patterns*
