begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|util
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|testclassification
operator|.
name|MiscTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|testclassification
operator|.
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * Tests LRUDictionary  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestLRUDictionary
block|{
name|LRUDictionary
name|testee
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|testee
operator|=
operator|new
name|LRUDictionary
argument_list|()
expr_stmt|;
name|testee
operator|.
name|init
argument_list|(
name|Short
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|TestContainsNothing
parameter_list|()
block|{
name|assertTrue
argument_list|(
name|isDictionaryEmpty
argument_list|(
name|testee
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Assert can't add empty array.    */
annotation|@
name|Test
specifier|public
name|void
name|testPassingEmptyArrayToFindEntry
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
argument_list|,
name|testee
operator|.
name|findEntry
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
argument_list|,
name|testee
operator|.
name|addEntry
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPassingSameArrayToAddEntry
parameter_list|()
block|{
comment|// Add random predefined byte array, in this case a random byte array from
comment|// HConstants.  Assert that when we add, we get new index.  Thats how it
comment|// works.
name|int
name|len
init|=
name|HConstants
operator|.
name|CATALOG_FAMILY
operator|.
name|length
decl_stmt|;
name|int
name|index
init|=
name|testee
operator|.
name|addEntry
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|index
operator|==
name|testee
operator|.
name|addEntry
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|index
operator|==
name|testee
operator|.
name|addEntry
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBasic
parameter_list|()
block|{
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|byte
index|[]
name|testBytes
init|=
operator|new
name|byte
index|[
literal|10
index|]
decl_stmt|;
name|rand
operator|.
name|nextBytes
argument_list|(
name|testBytes
argument_list|)
expr_stmt|;
comment|// Verify that our randomly generated array doesn't exist in the dictionary
name|assertEquals
argument_list|(
name|testee
operator|.
name|findEntry
argument_list|(
name|testBytes
argument_list|,
literal|0
argument_list|,
name|testBytes
operator|.
name|length
argument_list|)
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// now since we looked up an entry, we should have added it to the
comment|// dictionary, so it isn't empty
name|assertFalse
argument_list|(
name|isDictionaryEmpty
argument_list|(
name|testee
argument_list|)
argument_list|)
expr_stmt|;
comment|// Check if we can find it using findEntry
name|short
name|t
init|=
name|testee
operator|.
name|findEntry
argument_list|(
name|testBytes
argument_list|,
literal|0
argument_list|,
name|testBytes
operator|.
name|length
argument_list|)
decl_stmt|;
comment|// Making sure we do find what we're looking for
name|assertTrue
argument_list|(
name|t
operator|!=
operator|-
literal|1
argument_list|)
expr_stmt|;
name|byte
index|[]
name|testBytesCopy
init|=
operator|new
name|byte
index|[
literal|20
index|]
decl_stmt|;
name|Bytes
operator|.
name|putBytes
argument_list|(
name|testBytesCopy
argument_list|,
literal|10
argument_list|,
name|testBytes
argument_list|,
literal|0
argument_list|,
name|testBytes
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// copy byte arrays, make sure that we check that equal byte arrays are
comment|// equal without just checking the reference
name|assertEquals
argument_list|(
name|testee
operator|.
name|findEntry
argument_list|(
name|testBytesCopy
argument_list|,
literal|10
argument_list|,
name|testBytes
operator|.
name|length
argument_list|)
argument_list|,
name|t
argument_list|)
expr_stmt|;
comment|// make sure the entry retrieved is the same as the one put in
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|testBytes
argument_list|,
name|testee
operator|.
name|getEntry
argument_list|(
name|t
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|testee
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// making sure clear clears the dictionary
name|assertTrue
argument_list|(
name|isDictionaryEmpty
argument_list|(
name|testee
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|TestLRUPolicy
parameter_list|()
block|{
comment|//start by filling the dictionary up with byte arrays
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|Short
operator|.
name|MAX_VALUE
condition|;
name|i
operator|++
control|)
block|{
name|testee
operator|.
name|findEntry
argument_list|(
operator|(
name|BigInteger
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
operator|)
operator|.
name|toByteArray
argument_list|()
argument_list|,
literal|0
argument_list|,
operator|(
name|BigInteger
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
operator|)
operator|.
name|toByteArray
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|// check we have the first element added
name|assertTrue
argument_list|(
name|testee
operator|.
name|findEntry
argument_list|(
name|BigInteger
operator|.
name|ZERO
operator|.
name|toByteArray
argument_list|()
argument_list|,
literal|0
argument_list|,
name|BigInteger
operator|.
name|ZERO
operator|.
name|toByteArray
argument_list|()
operator|.
name|length
argument_list|)
operator|!=
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// check for an element we know isn't there
name|assertTrue
argument_list|(
name|testee
operator|.
name|findEntry
argument_list|(
name|BigInteger
operator|.
name|valueOf
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|toByteArray
argument_list|()
argument_list|,
literal|0
argument_list|,
name|BigInteger
operator|.
name|valueOf
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|toByteArray
argument_list|()
operator|.
name|length
argument_list|)
operator|==
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// since we just checked for this element, it should be there now.
name|assertTrue
argument_list|(
name|testee
operator|.
name|findEntry
argument_list|(
name|BigInteger
operator|.
name|valueOf
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|toByteArray
argument_list|()
argument_list|,
literal|0
argument_list|,
name|BigInteger
operator|.
name|valueOf
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|toByteArray
argument_list|()
operator|.
name|length
argument_list|)
operator|!=
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// test eviction, that the least recently added or looked at element is
comment|// evicted.  We looked at ZERO so it should be in the dictionary still.
name|assertTrue
argument_list|(
name|testee
operator|.
name|findEntry
argument_list|(
name|BigInteger
operator|.
name|ZERO
operator|.
name|toByteArray
argument_list|()
argument_list|,
literal|0
argument_list|,
name|BigInteger
operator|.
name|ZERO
operator|.
name|toByteArray
argument_list|()
operator|.
name|length
argument_list|)
operator|!=
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// Now go from beyond 1 to the end.
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|Short
operator|.
name|MAX_VALUE
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|testee
operator|.
name|findEntry
argument_list|(
name|BigInteger
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
operator|.
name|toByteArray
argument_list|()
argument_list|,
literal|0
argument_list|,
name|BigInteger
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
operator|.
name|toByteArray
argument_list|()
operator|.
name|length
argument_list|)
operator|==
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// check we can find all of these.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|Short
operator|.
name|MAX_VALUE
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|testee
operator|.
name|findEntry
argument_list|(
name|BigInteger
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
operator|.
name|toByteArray
argument_list|()
argument_list|,
literal|0
argument_list|,
name|BigInteger
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
operator|.
name|toByteArray
argument_list|()
operator|.
name|length
argument_list|)
operator|!=
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
specifier|private
name|boolean
name|isDictionaryEmpty
parameter_list|(
name|LRUDictionary
name|dict
parameter_list|)
block|{
try|try
block|{
name|dict
operator|.
name|getEntry
argument_list|(
operator|(
name|short
operator|)
literal|0
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|IndexOutOfBoundsException
name|ioobe
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
end_class

end_unit

