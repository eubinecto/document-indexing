# Report: Eu-Bin KIM (10327741)

## Abstract
Should I include an abstract? Like... something that outlines what I've implemented.
I've implemented ... and the pros & cons of each functionality is described below (with concrete examples! <- must mention this).
As for the design paradigm of MapReduce.. 

and.. show the search result for some queries below! (in diagram. which I'll keep refering back to 
in the body of the essay). 

## Functionality

### Case folding
  - selective folding -> I'm not doing this, no.
     - pros: ??? when would you need this though? when would you not want to lower all cases? 
  - lowering all cases (both for index and query)
    - pros: Start of the sentence, in the middle of the sentence -> can normalise them, and this is useful for
      (e.g. stopwords removal)
    - cons: Cannot disambiguate Proper noun from noun. (Can I come up with an example? or maybe a search example?)
      an example ->  " I went to New York". "I bought a new phone".  you cannot disambiguate New from  new. 
      but wait, isn't that a problem of multi-word expression? 
      - but think about "would your users ever use it". would the query... include any upper case letter(could cite statistics?)
      - e.g. CAT vs. cat. Apple vs. apple. proper pronoun.. but this downside could be compensated with the relationships
      with other query terms. 

### Stopwords filtering
- **filtering out stopwords**
  - pros: filter out semantically less important terms. More accurate search. 
  - con: but what if the query was quoting a specific line in the episodes? come up with this. this is actually the most
    important part.
    
  
### stemming
  - how? do you stem all the words? or do conditional stemming?
  - pros: ??
  - cons: ??


### Domain-specific filtering
 - how?  -> Using regular expressions. They are implemented as chained java stream filters. in line..(.. 
 - what? -> s
    - filtering out numbers.
    - filtering out isbn.
    - filtering out dates.
 - why? -> the domain of the data matters (tokenisation - slide  9), because.. punctuation
 - limitations -> e.g. Not good if numbers are used as Proper Noun (e.g. the book 1984).



```
allow|2	[Bart_the_Fink.txt.gz|3|[423, 548, 603], Bart_the_Lover.txt.gz|1|[581]]
alter|2	[Bart_the_Fink.txt.gz|2|[782, 967], Bart_the_Lover.txt.gz|2|[155, 329]]
```
**Figure 2** : Inverted index for `allow` and `alter`. Document Frequency (DF, next to each term), Term Frequency
 (TF, next to each document) and positional index (next to TF) are stored. 

### Positional Indexing
Positional Indexing is implemented in `inMapperAggreagtion` method (line 205-221), which utilises `Counter` to keep 
track of term positions. A Positional Index specifies (Christopher D. Manning, et.al, 2008) the positions at which
 the term appears in the postings. For example, *Figure 2* above shows the positional index for `allow`; 
 the term `allow` appears at 423rd, 548th and 603rd positions in the document `Bart_the_Fink.txt.gz`.
   Such explicit specification of term positions is useful when one wants to implement
efficient proximity search. That is, if we were to search "allow to alter" on the index shown in *Figure 2*,  for instance,
we can efficiently work out that the two terms appear closer in `Bart_the_Fink.txt.gz` than in
 `Bart_the_Lover.txt.gz`  (782 - 603 = 179 < 581 - 329 = 252), and that the former should be more relevant to the query than
 the latter.
 
### DF & TF
   - how did you do it?
  - what is it (as for this, I know what to cite)
 - how does it improve the index -> show a real example using your index.
  

## Performance


design pattern | Finish log
--- | --- 
without in-mapper aggregation | `20/11/24 14:20:43 INFO exercise.BasicInvertedIndex: Job Finished in 22.782 seconds`.
with in-mapper aggregation | `20/11/24 10:37:21 INFO exercise.BasicInvertedIndex: Job Finished in 22.549 seconds`

As for MapReduce design pattern, In-mapper aggregation pattern is used in the code. 

If there are many duplicated terms in each file split, then MapReduce jobs with in-mapper aggregation design would be faster than 
those without aggregations. running `BasicInvertedIndex.java` without in-mapper
  - cons: Not so much useful if there aren't that many duplicated terms in each line.
  - without combiner: took 
  - with combiner: took 
  - would be great if I could mathematically approximate the time... and then compare to how long it actually took.
  
When there is a bottle neck between mappers & reducers. E.g. mappers produces 10000 key-value pairs,
but there are only 3 reducers available.



## References
- (Christopher D. Manning, et.al, 2008) , *Introduction to Information Retrieval*
  - entry: https://nlp.stanford.edu/IR-book/html/htmledition/irbook.html
  - positional indexing: https://nlp.stanford.edu/IR-book/html/htmledition/positional-indexes-1.html
