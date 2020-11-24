# Report: Eu-Bin KIM (10327741)

## Abstract
Should I include an abstract? Like... something that outlines what I've implemented.
I've implemented ... and the pros & cons of each functionality is described below (with concrete examples! <- must mention this).
As for the design paradigm of MapReduce.. 

and.. show the search result for some queries below! (in diagram. which I'll keep refering back to 
in the body of the essay). 



## Functionality
> What features were implemented, what did they do to improve the index, and what problems may they create?
> What are the limitations?

- Case folding
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
)
- filtering out stopwords
  - pros: filter out semantically less important terms. More accurate search. 
  - con: but what if the query was quoting a specific line in the episodes? come up with this. this is actually the most
    important part.
    
- cleansing terms
  - how: write the regular expression here.
  - pros: ...
  - cons: ...
- stemming
  - how? do you stem all the words? or do conditional stemming?
  - pros: ??
  - cons: ??

 - the domain of the data matters (tokenisation - slide  9), because.. punctuation 


## Performance
> What MapReduce design patterns were used in the code, and what effect did they have on the performance of the program?
> What factors are likely to limit the performance of your application, and why?

Note: MapReduce design patterns: Book work. Refer to the slides. 
- in-mapper aggregation with summarization (aka Combiner pattern)
  - pros: overcomes the performance limitations of the previous design (what is this design called?)
  - cons: Not so much useful if there aren't that many duplicated terms in each line.
  - without combiner: took `.... time`.
  - with combiner: took `20/11/24 10:37:21 INFO exercise.BasicInvertedIndex: Job Finished in 22.549 seconds`
  - would be great if I could mathematically approximate the time... and then compare to how long it actually took.
  
When there is a bottle neck between mappers & reducers. E.g. mappers produces 10000 key-value pairs,
but there are only 3 reducers available.


