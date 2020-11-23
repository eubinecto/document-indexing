# Report: Eu-Bin KIM (10327741)

## Functionality
> What features were implemented, what did they do to improve the index, and what problems may they create?
> What are the limitations?

- lowering all cases
  - pros: Start of the sentence, in the middle of the sentence -> can normalise them
  - cons: Cannot disambiguate Proper noun from noun. (Can I come up with an example? or maybe a search example?)
- filtering out stopwords
  - pros: filter out semantically less important terms. More accurate search. 
  - con: but what if the query was quoting a specific line in the episodes? come up with this.
 

Back your argument with evidence. Use evaluation metrics. (e.g. NDCG). Hand-lable docs for a specific query, 
As for MapReduce; two attempts have been made.



## Performance
### What MapReduce design patterns were used in the code, and what effect did they have on the performance of the program?
Note: MapReduce design patterns: Book work. Refer to the slides. 


### What factors are likely to limit the performance of your application, and why?




## How should you write the report? (answers to the questions raised in the lab)
- focus on the depth. 
- word count matters. get rid of the questions once you are done with the answers.
- for in-depth analysis, you must provide examples. (For both to prove something is useful
  as well as something is not).
- do some book work?
come up with a motivation for every feature you put in. Don't forget to be critical.
- collect all the output variations, and show different search results for the same query
