########################################################################################
# Binary Paint Problem 
#
# USAGE:
# python PaintProblem <input-file>
#
# This python function reads in the input file, converting each customer colours/type
# selection to a binary list, with 1 being matte paint and 0 being glossy. In order to 
# evaluate which colour/type selection will work, this program iterates over a constrained
# subset of possible binary lists, accepting or rejecting lists if they don't satisfy each
# customer. 
#
# Shane Brennan
# Version: 0.1
# Date: 30th May 2015
########################################################################################

#!/usr/bin/env python
import sys
import os
import time
import itertools

class TestCase:
	def __init__(self, testCaseNum):
		""" Initialize the lists used to parse the 
		    colours, types and assigned customer ID's.
		"""
		# The counters for the test-case
		self.caseNum = (testCaseNum+1)
		self.numCustomers = 0
		self.numColours = 0

		# The binary array of colour types for each customer
		self.types = []
		# The bitmask for each customer's colour selection
		self.masks = []

		# The overall bitmask for all colours selected by all customers
		self.bitmask = []
		self.totalSetBits = 0

	def evaluate(self):
		""" Checks the constraints, based on the existing colours/types assigned from 
		    previous customer selections. 
		"""
		# The list of valid selections to populate
		validSelectionList = []

		# Get a reduced subset of the possible combinations
		combinations = list(itertools.product([0, 1], repeat=self.totalSetBits))

		# Iterate through the list of possible combinations
		for testCombo in combinations:
			testList = self.getTestList(testCombo)

			# Selection must be valid for all clients
			validSelection = True
			for typeIndex, typeList in enumerate(self.types):
				customerBitMask = self.masks[typeIndex]
				validSelection = self.checkSelection(testList, typeList, customerBitMask)
				# If any customer isn't a match, finish searching on this combo
				if validSelection == False:
					break

			# If it's a valid combo of colours/types, add it to the list (in case of multiple matches)
			if validSelection == True and testList not in validSelectionList:
				validSelectionList.append(testList)


		if len(validSelectionList) == 0:
			print 'Case #{0}: IMPOSSIBLE'.format(self.caseNum)
		else:
			# In case there's multiple possibilities, find the one which
			# minimizes the matte (1) colours
			maxMatteColours = self.numColours
			result = validSelectionList[0]

			for validList in validSelectionList:
				matteColours = self.countBits(validList, 1)
				if matteColours < maxMatteColours:
					maxMatteColours = matteColours
					result = validList

			# Now print the output
			outputString = ' '.join(str(x) for x in result)
			print 'Case #{0}: {1}'.format(self.caseNum, outputString)

	def getTestList(self, testList):
		""" The test list is a row in the iterated binary list, i.e, 0,0,0 0,0,1 0,1,0 etc.
		    Since we try to minimize the number of combinations, only those colours which 
		    change between all customers are iterated. So if there were five colours in total 
		    but only two changed, we'd iterate over 0,0 0,1 1,0 1,1 and re-create the iterated
		    colour type list in full by putting the points of change in at the appropriate index. 
		    
		    Inputs: a shortened binary list, e.g., [0,1]
		    Outputs: the full-length binary list, e.g, [0,0,0,0,0,0,1]
		"""

		resultList = [0] * self.numColours
		testIndex = 0
		for index, mask in enumerate(self.bitmask):
			if mask == 1:
				resultList[index] = testList[testIndex]
				testIndex += 1

		return resultList

	def checkSelection(self, testList, customerTypes, customerBitMask):
		for index, mask in enumerate(customerBitMask):
			if mask == 1:
				if testList[index] == customerTypes[index]:
					return True
		return False


	def countBits(self, testList, value):
		""" Counts the number of bits (1/0) set to a particular 
		    value in the provided binary list. 
		"""
		result = 0
		for bit in testList:
			if bit == value:
				result += 1
		return result

	def setColours(self, value):
		""" Sets up the colours and associated lists
		"""
		self.numColours = value
		self.bitmask = [0] * self.numColours

	def hasColours(self):
		""" Returns true when the Test Case has 
		    some colours defined. 
		"""
		if self.numColours > 0:
			return True
		return False

	def setCustomers(self, value):
		self.numCustomers = value

	def hasCustomers(self):
		""" Returns true when the Test Case has 
		    some customers defined. 
		"""
		if self.numCustomers > 0:
			return True
		return False
		
	def addCustomerPaints(self, line):
		""" Takes in a line, e.g., 1 5 0 and converts it into two lists
		    the first a binary list representing the colours/type selection
		    with the index position being the colour and the value (1/0) being
		    the type. The second list created is a bitmask showing which colour
		    indexes were set. 
		"""
		tokens = line.split(' ')
		length = int(tokens[0])
		
		# Removes the first (length) value from the line
		tokens = tokens[1:]

		# Error check the lenght as provided in the line
		if length != (len(tokens)/2):
			print "Error, invalid length for paint types -",line
		else:
			# Parse the line into two lists
			customerTypes = []
			customerColours = []

			for token in tokens:
				value = int(token.rstrip())
				if len(customerColours) == len(customerTypes):
					customerColours.append(value)
				else:
					customerTypes.append(value)

			# Initially set the lists to n-sized zero-filled lists 
			typeList = [0] * self.numColours
			maskList = [0] * self.numColours

			for index, colour in enumerate(customerColours):
				maskList[colour-1] = 1
				type = customerTypes[index]
				if type == 1:
					typeList[colour-1] = 1
		
			# The colour types per customer, i.e., 1 or 0 for matte/glossy
			self.types.append(typeList)
			self.masks.append(maskList)

			# Generates a list of all the values which vary
			for index, bit in enumerate(maskList):
				if bit == 1 and self.bitmask[index] != 1:
					self.bitmask[index] = 1
					self.totalSetBits += 1


### The PaintProblem class, used to perform the I/O and instantiate Test-Cases ###

class PaintProblem:

	def __init__(self, filename, verbose=False):
		""" The constructor for the main reader class, this 
		    function is used to instantiate each TestCase.
		"""
		# Open the input file for reading
		file = open(filename, "r")

		# List to store test-cases
		self.testCases = []
		testcase = TestCase(len(self.testCases))
		self.testCases.append(testcase)

		# Counter to read the input and customer data
		lineNum = 0
		customerIndex = 0
		testCaseIndex = 0

		# Read the file line by line
		for line in file:
			# The tokens in the file
			tokens = line.rstrip().split(" ")
		
			# If the line number is zero
			if lineNum == 0:
				self.numTestCases = int(line.rstrip())
			else:
				# If there's a single value
				if len(tokens) == 1:

					# Check if the test case has been instantiated
					colours = self.testCases[-1].hasColours()
					customers = self.testCases[-1].hasCustomers()
					customerIndex = 0			
					testCaseIndex = 0

					# Add another test case if new data specified
					if colours == True and customers == True:
						# Evaluate the previous test-case
						if verbose:
							self.testCases[-1].printTestCase()
						self.testCases[-1].evaluate()

						# Create a new test-case
						testcase = TestCase(len(self.testCases))
						testcase.setColours(int(line.rstrip()))
						self.testCases.append(testcase)
						colours = self.testCases[-1].hasColours
						customers = self.testCases[-1].hasCustomers
						customerIndex += 1	
						testCaseIndex += 1

					# Otherwise add the colours
					if colours == False and customers == False:
						self.testCases[-1].setColours(int(line.rstrip()))

					# Then add the customers
					if colours == True and customers == False:
						self.testCases[-1].setCustomers(int(line.rstrip()))
				else:
					# If there's multiple tokens add the paint colours/types
					# This function also checks the constraints as it processes customers. 
					self.testCases[-1].addCustomerPaints(line.rstrip())				
			lineNum += 1

		# Evaluate the final test-case
		if verbose:
			self.testCases[-1].printTestCase()
		self.testCases[-1].evaluate()

def printUsage():
	print "DESCRIPTION:"
	print "Finds the minimum number of paint batches which satify the constraints.\n"
	print "USAGE:\n"
	print "PaintProblem [-v|--verbose] <input-file>"

def main(argv):
	if len(argv) == 2:
		filename = argv[1]
		PaintProblem(filename, False)
		return 0
	elif len(argv) == 3:
		verbose = argv[1]
		filename = argv[2]
		if verbose == '-v' or verbose == '--verbose':
			PaintProblem(filename, True)
			return 0
		else:
			printUsage()
			return 1
	else :
		printUsage()
        	return 1

if __name__ == "__main__":
    sys.exit(main(sys.argv))
	
