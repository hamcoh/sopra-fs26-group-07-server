import unittest
from sample_problem_solution import solution_sample_problem

class TestSampleProblem(unittest.TestCase):

    def test_happy(self):
        self.assertEqual(solution_sample_problem("java"), "avaj")

    def test_happy_longer(self):
        self.assertEqual(solution_sample_problem("pythonic"), "cinohtyp")

    def test_single_char(self):
        self.assertEqual(solution_sample_problem("a"), "a")

    def test_palindrome(self):
        self.assertEqual(solution_sample_problem("ekitike"), "ekitike")

    def test_two_chars(self):
        self.assertEqual(solution_sample_problem("in"), "ni")

if __name__ == "__main__":
    unittest.main()