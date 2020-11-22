# code reference: https://medium.com/@fro_g/writing-a-simple-inverted-index-in-python-3c8bcb52169a
from typing import List, Dict, Tuple
from dataclasses import dataclass
import re
import pprint
from termcolor import colored

texts = [
    "The big sharks of Belgium drink beer.",
    "Belgium has great beer. They drink beer all the time."
]


# represents a document
@dataclass
class Document:
    id: int
    text: str


@dataclass
class Entry:
    doc_freq: int
    # list of doc ids
    postings_list: List[int]


class Tokenizer:
    @staticmethod
    def tokenize(text: str) -> List[str]:
        cleaned_text = re.sub(r'[^\w\s]', '', text)
        # use simple white-space tokenizer, as of right now.
        return cleaned_text.split(" ")


class Indexer:
    @staticmethod
    def fetch_docs(docs: List[Document]) -> List[Tuple[str, int]]:
        """
        "Indexer steps: Token sequence"
        generates a sequence of (Modified token, doc_id), ("doc_tuples")
        :param docs:
        :return:
        """
        return [
            (token, doc.id)  # (modified token, doc_id)
            for doc in docs  # for each doc
            for token in Tokenizer.tokenize(doc.text)  # for each tokenized term
        ]  # return as a list, as of right now

    @staticmethod
    def sort_doc_tuples(doc_tuples: List[Tuple[str, int]]) -> List[Tuple[str, int]]:
        """
        "Indexer steps: Sort"
        :param doc_tuples: a sequence of (modified token, doc_id)
        :return:
        """
        # sort first by terms,
        # then sort by doc id (for group with the same terms)
        # use secondary sorting...?
        return sorted(doc_tuples,
                      # (primary key, secondary key)
                      key=lambda doc_tuple: (doc_tuple[0], doc_tuple[1]))

    @staticmethod
    def index_docs(inverted_index: Dict[str, Entry], docs: List[Document]):
        """
        "indexer steps: Dictionary & Postings"
        :param inverted_index:
        :param docs:
        :return:
        """
        pp = pprint.PrettyPrinter(indent=4)
        doc_tuples = Indexer.fetch_docs(docs)  # step 1
        print(colored("doc_tuples:", 'blue'))
        pp.pprint(doc_tuples)
        doc_tuples_sorted = Indexer.sort_doc_tuples(doc_tuples)  # step 2
        print(colored("doc_tuples_sorted:", 'blue'))
        pp.pprint(doc_tuples_sorted)
        # build the inverted index
        for term, doc_id in doc_tuples_sorted:
            if term in inverted_index:
                inverted_index[term].doc_freq += 1
                inverted_index[term].postings_list.append(doc_id)
            else:
                inverted_index[term] = Entry(doc_freq=1, postings_list=[doc_id])


# in-memory Database. Just for experimental purpose
class Database:
    def __init__(self):
        # documents
        self.docs: Dict[int, Document] = dict()
        # inverted indices for the documents
        self.inverted_index: Dict[str, Entry] = dict()

    def add(self, docs: List[Document]):
        for doc in docs:
            self.docs[doc.id] = doc

    def get(self, doc_id: int) -> Document:
        return self.docs[doc_id]

    def build_inverted_index(self):
        docs = [doc for _, doc in self.docs.items()]
        # index documents using the indexer
        Indexer.index_docs(self.inverted_index, docs)

    def search_docs(self, query: str):
        # make sure you've built the index first.
        # this is a very naive search.
        query_terms = Tokenizer.tokenize(query)
        results = {
            # returns the postings list for the query
            query_term: self.inverted_index[query_term]
            for query_term in query_terms
            if query_term in self.inverted_index
        }
        for term, entry in results.items():
            for doc_id in set(entry.postings_list):
                # print out search results
                doc = self.get(doc_id)
                highlighted = doc.text.replace(term, colored(term, 'magenta'))
                print(highlighted)
            print("-----")


def main():
    global texts
    # build docs
    docs = [Document(doc_id, text) for doc_id, text in enumerate(texts)]
    # instantiate a database
    db = Database()
    db.add(docs)
    # build inverted indices
    db.build_inverted_index()
    db.search_docs("drink beer")


if __name__ == '__main__':
    main()
