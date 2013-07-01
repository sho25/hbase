begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|filter
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|*
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
name|client
operator|.
name|Put
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
name|client
operator|.
name|Scan
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
name|filter
operator|.
name|CompareFilter
operator|.
name|CompareOp
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
name|filter
operator|.
name|Filter
operator|.
name|ReturnCode
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|InternalScanner
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
name|After
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestDependentColumnFilter
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|ROWS
init|=
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test1"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test2"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|FAMILIES
init|=
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"familyOne"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"familyTwo"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|STAMP_BASE
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
index|[]
name|STAMPS
init|=
block|{
name|STAMP_BASE
operator|-
literal|100
block|,
name|STAMP_BASE
operator|-
literal|200
block|,
name|STAMP_BASE
operator|-
literal|300
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|BAD_VALS
init|=
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bad1"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bad2"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bad3"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|MATCH_VAL
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"match"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|testVals
decl_stmt|;
specifier|private
name|HRegion
name|region
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
name|testVals
operator|=
name|makeTestVals
argument_list|()
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd0
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|hcd0
operator|.
name|setMaxVersions
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd0
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|hcd1
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILIES
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|hcd1
operator|.
name|setMaxVersions
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd1
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|this
operator|.
name|region
operator|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|info
argument_list|,
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|htd
argument_list|)
expr_stmt|;
name|addData
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|this
operator|.
name|region
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addData
parameter_list|()
throws|throws
name|IOException
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROWS
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
comment|// add in an entry for each stamp, with 2 as a "good" value
name|put
operator|.
name|add
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIER
argument_list|,
name|STAMPS
index|[
literal|0
index|]
argument_list|,
name|BAD_VALS
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIER
argument_list|,
name|STAMPS
index|[
literal|1
index|]
argument_list|,
name|BAD_VALS
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIER
argument_list|,
name|STAMPS
index|[
literal|2
index|]
argument_list|,
name|MATCH_VAL
argument_list|)
expr_stmt|;
comment|// add in entries for stamps 0 and 2.
comment|// without a value check both will be "accepted"
comment|// with one 2 will be accepted(since the corresponding ts entry
comment|// has a matching value
name|put
operator|.
name|add
argument_list|(
name|FAMILIES
index|[
literal|1
index|]
argument_list|,
name|QUALIFIER
argument_list|,
name|STAMPS
index|[
literal|0
index|]
argument_list|,
name|BAD_VALS
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILIES
index|[
literal|1
index|]
argument_list|,
name|QUALIFIER
argument_list|,
name|STAMPS
index|[
literal|2
index|]
argument_list|,
name|BAD_VALS
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|this
operator|.
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROWS
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIER
argument_list|,
name|STAMPS
index|[
literal|0
index|]
argument_list|,
name|BAD_VALS
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|// there is no corresponding timestamp for this so it should never pass
name|put
operator|.
name|add
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIER
argument_list|,
name|STAMPS
index|[
literal|2
index|]
argument_list|,
name|MATCH_VAL
argument_list|)
expr_stmt|;
comment|// if we reverse the qualifiers this one should pass
name|put
operator|.
name|add
argument_list|(
name|FAMILIES
index|[
literal|1
index|]
argument_list|,
name|QUALIFIER
argument_list|,
name|STAMPS
index|[
literal|0
index|]
argument_list|,
name|MATCH_VAL
argument_list|)
expr_stmt|;
comment|// should pass
name|put
operator|.
name|add
argument_list|(
name|FAMILIES
index|[
literal|1
index|]
argument_list|,
name|QUALIFIER
argument_list|,
name|STAMPS
index|[
literal|1
index|]
argument_list|,
name|BAD_VALS
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|this
operator|.
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|KeyValue
argument_list|>
name|makeTestVals
parameter_list|()
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|testVals
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|testVals
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|ROWS
index|[
literal|0
index|]
argument_list|,
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIER
argument_list|,
name|STAMPS
index|[
literal|0
index|]
argument_list|,
name|BAD_VALS
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|testVals
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|ROWS
index|[
literal|0
index|]
argument_list|,
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIER
argument_list|,
name|STAMPS
index|[
literal|1
index|]
argument_list|,
name|BAD_VALS
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|testVals
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|ROWS
index|[
literal|0
index|]
argument_list|,
name|FAMILIES
index|[
literal|1
index|]
argument_list|,
name|QUALIFIER
argument_list|,
name|STAMPS
index|[
literal|1
index|]
argument_list|,
name|BAD_VALS
index|[
literal|2
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|testVals
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|ROWS
index|[
literal|0
index|]
argument_list|,
name|FAMILIES
index|[
literal|1
index|]
argument_list|,
name|QUALIFIER
argument_list|,
name|STAMPS
index|[
literal|0
index|]
argument_list|,
name|MATCH_VAL
argument_list|)
argument_list|)
expr_stmt|;
name|testVals
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|ROWS
index|[
literal|0
index|]
argument_list|,
name|FAMILIES
index|[
literal|1
index|]
argument_list|,
name|QUALIFIER
argument_list|,
name|STAMPS
index|[
literal|2
index|]
argument_list|,
name|BAD_VALS
index|[
literal|2
index|]
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|testVals
return|;
block|}
comment|/**    * This shouldn't be confused with TestFilter#verifyScan    * as expectedKeys is not the per row total, but the scan total    *    * @param s    * @param expectedRows    * @param expectedCells    * @throws IOException    */
specifier|private
name|void
name|verifyScan
parameter_list|(
name|Scan
name|s
parameter_list|,
name|long
name|expectedRows
parameter_list|,
name|long
name|expectedCells
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalScanner
name|scanner
init|=
name|this
operator|.
name|region
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
name|int
name|cells
init|=
literal|0
decl_stmt|;
for|for
control|(
name|boolean
name|done
init|=
literal|true
init|;
name|done
condition|;
name|i
operator|++
control|)
block|{
name|done
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|results
operator|.
name|toArray
argument_list|(
operator|new
name|KeyValue
index|[
name|results
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"counter="
operator|+
name|i
operator|+
literal|", "
operator|+
name|results
argument_list|)
expr_stmt|;
if|if
condition|(
name|results
operator|.
name|isEmpty
argument_list|()
condition|)
break|break;
name|cells
operator|+=
name|results
operator|.
name|size
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Scanned too many rows! Only expected "
operator|+
name|expectedRows
operator|+
literal|" total but already scanned "
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|,
name|expectedRows
operator|>
name|i
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Expected "
operator|+
name|expectedCells
operator|+
literal|" cells total but "
operator|+
literal|"already scanned "
operator|+
name|cells
argument_list|,
name|expectedCells
operator|>=
name|cells
argument_list|)
expr_stmt|;
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Expected "
operator|+
name|expectedRows
operator|+
literal|" rows but scanned "
operator|+
name|i
operator|+
literal|" rows"
argument_list|,
name|expectedRows
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Expected "
operator|+
name|expectedCells
operator|+
literal|" cells but scanned "
operator|+
name|cells
operator|+
literal|" cells"
argument_list|,
name|expectedCells
argument_list|,
name|cells
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test scans using a DependentColumnFilter    */
annotation|@
name|Test
specifier|public
name|void
name|testScans
parameter_list|()
throws|throws
name|Exception
block|{
name|Filter
name|filter
init|=
operator|new
name|DependentColumnFilter
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIER
argument_list|)
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|verifyScan
argument_list|(
name|scan
argument_list|,
literal|2
argument_list|,
literal|8
argument_list|)
expr_stmt|;
comment|// drop the filtering cells
name|filter
operator|=
operator|new
name|DependentColumnFilter
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIER
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|verifyScan
argument_list|(
name|scan
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// include a comparator operation
name|filter
operator|=
operator|new
name|DependentColumnFilter
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIER
argument_list|,
literal|false
argument_list|,
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|MATCH_VAL
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
comment|/*      * expecting to get the following 3 cells      * row 0      *   put.add(FAMILIES[0], QUALIFIER, STAMPS[2], MATCH_VAL);      *   put.add(FAMILIES[1], QUALIFIER, STAMPS[2], BAD_VALS[2]);      * row 1      *   put.add(FAMILIES[0], QUALIFIER, STAMPS[2], MATCH_VAL);      */
name|verifyScan
argument_list|(
name|scan
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// include a comparator operation and drop comparator
name|filter
operator|=
operator|new
name|DependentColumnFilter
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIER
argument_list|,
literal|true
argument_list|,
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|MATCH_VAL
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
comment|/*      * expecting to get the following 1 cell      * row 0      *   put.add(FAMILIES[1], QUALIFIER, STAMPS[2], BAD_VALS[2]);      */
name|verifyScan
argument_list|(
name|scan
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that the filter correctly drops rows without a corresponding timestamp    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testFilterDropping
parameter_list|()
throws|throws
name|Exception
block|{
name|Filter
name|filter
init|=
operator|new
name|DependentColumnFilter
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIER
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|accepted
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|val
range|:
name|testVals
control|)
block|{
if|if
condition|(
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|val
argument_list|)
operator|==
name|ReturnCode
operator|.
name|INCLUDE
condition|)
block|{
name|accepted
operator|.
name|add
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|"check all values accepted from filterKeyValue"
argument_list|,
literal|5
argument_list|,
name|accepted
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|filter
operator|.
name|filterRow
argument_list|(
name|accepted
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"check filterRow(List<KeyValue>) dropped cell without corresponding column entry"
argument_list|,
literal|4
argument_list|,
name|accepted
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// start do it again with dependent column dropping on
name|filter
operator|=
operator|new
name|DependentColumnFilter
argument_list|(
name|FAMILIES
index|[
literal|1
index|]
argument_list|,
name|QUALIFIER
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|accepted
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|KeyValue
name|val
range|:
name|testVals
control|)
block|{
if|if
condition|(
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|val
argument_list|)
operator|==
name|ReturnCode
operator|.
name|INCLUDE
condition|)
block|{
name|accepted
operator|.
name|add
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|"check the filtering column cells got dropped"
argument_list|,
literal|2
argument_list|,
name|accepted
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|filter
operator|.
name|filterRow
argument_list|(
name|accepted
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"check cell retention"
argument_list|,
literal|2
argument_list|,
name|accepted
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test for HBASE-8794. Avoid NullPointerException in DependentColumnFilter.toString().    */
annotation|@
name|Test
specifier|public
name|void
name|testToStringWithNullComparator
parameter_list|()
block|{
comment|// Test constructor that implicitly sets a null comparator
name|Filter
name|filter
init|=
operator|new
name|DependentColumnFilter
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIER
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|filter
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"check string contains 'null' as compatator is null"
argument_list|,
name|filter
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"null"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test constructor with explicit null comparator
name|filter
operator|=
operator|new
name|DependentColumnFilter
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIER
argument_list|,
literal|true
argument_list|,
name|CompareOp
operator|.
name|EQUAL
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|filter
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"check string contains 'null' as compatator is null"
argument_list|,
name|filter
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"null"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testToStringWithNonNullComparator
parameter_list|()
block|{
name|Filter
name|filter
init|=
operator|new
name|DependentColumnFilter
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|QUALIFIER
argument_list|,
literal|true
argument_list|,
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|MATCH_VAL
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|filter
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"check string contains comparator value"
argument_list|,
name|filter
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"match"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

