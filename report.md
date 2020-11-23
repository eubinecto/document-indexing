# Report: Eu-Bin KIM (10327741)

## Functionality
> What features were implemented, what did they do to improve the index, and what problems may they create?
> What are the limitations?

- Case folding
  - selective folding
     - pros: ??? when would you need this though? when would you not want to lower all cases? 
  - lowering all cases
    - pros: Start of the sentence, in the middle of the sentence -> can normalise them, and this is useful for
      (e.g. stopwords removal)
    - cons: Cannot disambiguate Proper noun from noun. (Can I come up with an example? or maybe a search example?)
      an example ->  " I went to New York". "I bought a new phone".  you cannot disambiguate New from  new. 
      but wait, isn't that a problem of multi-word expression? 
- filtering out stopwords
  - pros: filter out semantically less important terms. More accurate search. 
  - con: but what if the query was quoting a specific line in the episodes? come up with this.
- stemming
  - pros: ??
  - cons: ??
 


## Performance
> What MapReduce design patterns were used in the code, and what effect did they have on the performance of the program?
> What factors are likely to limit the performance of your application, and why?

Note: MapReduce design patterns: Book work. Refer to the slides. 
- in-mapper aggregation with summarization 
  - pros: overcomes the performance limitations of the previous design (what is this design called?)
  - cons: Not so much useful if there aren't that many duplicated terms in each line.
  
When there is a bottle neck between mappers & reducers. E.g. mappers produces 10000 key-value pairs,
but there are only 3 reducers available.


## How should you write the report? (answers to the questions raised in the lab)
- Back your argument with evidence. Use evaluation metrics. (e.g. NDCG). Hand-lable docs for a specific query, 
As for MapReduce; two attempts have been made.
- focus on the depth. -> provide concrete examples, wherever you can
- Make sure to keep it brief. Brevity is the hardest part of all.
- word count matters. get rid of the questions once you are done with the answers.
- for in-depth analysis, you must provide examples. (For both to prove something is useful
  as well as something is not).
- do some book work?
come up with a motivation for every feature you put in. Don't forget to be critical.
- collect all the output variations, and show different search results for the same query
- make sure you cite the slides!
