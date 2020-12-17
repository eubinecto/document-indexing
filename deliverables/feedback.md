## Feeback - the marking rubric
> got 24 / 30
Put in the image here...


## Feedback - his email
```
Coursework 1 for COMP38211 has now been marked, and the marks and feedback are available. The feedback is being provided via a Blackboard Rubric, and should explain how marks were obtained and where marks were lost.

We hope that the lab has been useful, in providing experience of a new programming model for big data, as well as allowing experimentation with constructs for searching on the web.

The average grade is around 65%, with about 42% of the submissions over 70%, and thus clearly in the 1st class honours range. However, about 7% of the submissions are below 50%, so these folks will really want to understand what went wrong before submitting Coursework 2.

Where were marks earned/lost in general?

• There are 4 marks for implementing the mandatory functionality; almost everyone got all of these.
• There are 4 marks for additional functionality. The positional indexing mark here is quite hard to get, as the solution must work over file splits.  Full marks here depend on doing something bespoke, and not just linking together the routines provided.
• There are 4 marks for the suitability of the implementation. Most people lost some of the implementation related marks, for example:
• through providing implementations with some missing or broken functionalities; or
• through some feature of the implementation falling short in some way (e.g.  literals in the code for the number of documents in a collection, or the use of strings for describing structured results).
• In the discussion, for each topic there was a sliding scale of marks based on the quality of the discussion. We marked the best two or three discussions on functionalities and best one or two performance discussions provided. Describing what you did without justification, or giving basic bookwork, was not a route to high marks.  Better marks came from correctly identifying limitations, illustrating issues using The Simpsons data, and proposing solutions to limitations.

Although separate marks are given for the code and the report, in fact these are not completely independent. So, for example, if you explained a potentially dubious design decision reasonably in the report this may have saved you from losing a mark in the code.

We hope that these comments are useful. There will be a chance to discuss specific questions about your Coursework 1 submissions at the lab on Friday.

Regards,

Goran / Norman

```



## Reflection
### Positional Indexing
The implementation of Positional indexing is wrong. It is wrong in two ways. First, You are using a `Counter` object, but that only works
if the file splits are sorted, which they are not. The correct way of doing it is to get use of `getStart()` method of 
`FileSplit` class:
```java
long start_idx = ((FileSplit) context.getInputSplit()).getStart();
```
Secondly, you are tokenzing terms before you build positional index. This might depend on the domain, though.

Reflection:
- Don't make assumption on what you are supposed to do. Read the instuctions, and **ask questions** if you are in any doubt.


### Flagging Important terms
You haven't implemented flagging important terms. I remember it was in the script, but then I thought
it was just one of the things I could do. The way you do this should have been simple: compute TF*IDF for each 
term in `Reduce` before you emit, and set the terms with TF*IDF > `threshold` to True, False otherwise.

Reflection: Read the instructions properly!

### TF & IDF 

- should have been more critical on TF & IDF. You've just provided an explanation of it,
which they already know. You should have included points & concrete examples in relation to
why TF & IDF could be limited in some situations (could have been good if you could draw 
its limits in relation to Simpson's Episodes)


Reflection: All of the functionalities had the same marking criteria. Why did you expect mentioning just
 book work would be okay as far as TF * IDF is concerned? follow the instructions.
 
 
 
### Map-Reduce Design pattern
For this, I don't really get why I did not get full marks. How do I get full marks for this? 
