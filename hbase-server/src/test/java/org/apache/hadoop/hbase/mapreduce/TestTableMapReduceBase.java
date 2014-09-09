begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapreduce
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
name|assertTrue
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
name|fail
import|;
end_import

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
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableMap
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
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|Cell
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
name|CellUtil
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
name|HBaseTestingUtility
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
name|client
operator|.
name|HTable
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
name|Result
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
name|ResultScanner
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
name|client
operator|.
name|Table
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
name|io
operator|.
name|ImmutableBytesWritable
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
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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

begin_comment
comment|/**  * A base class for a test Map/Reduce job over HBase tables. The map/reduce process we're testing  * on our tables is simple - take every row in the table, reverse the value of a particular cell,  * and write it back to the table. Implements common components between mapred and mapreduce  * implementations.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|TestTableMapReduceBase
block|{
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|MULTI_REGION_TABLE_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"mrtest"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|INPUT_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"contents"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|OUTPUT_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"text"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|columns
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|INPUT_FAMILY
block|,
name|OUTPUT_FAMILY
block|}
decl_stmt|;
comment|/**    * Retrieve my logger instance.    */
specifier|protected
specifier|abstract
name|Log
name|getLog
parameter_list|()
function_decl|;
comment|/**    * Handles API-specifics for setting up and executing the job.    */
specifier|protected
specifier|abstract
name|void
name|runTestOnTable
parameter_list|(
name|HTable
name|table
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|HTable
name|table
init|=
name|UTIL
operator|.
name|createTable
argument_list|(
name|MULTI_REGION_TABLE_NAME
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|INPUT_FAMILY
block|,
name|OUTPUT_FAMILY
block|}
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|createMultiRegions
argument_list|(
name|table
argument_list|,
name|INPUT_FAMILY
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|INPUT_FAMILY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniMapReduceCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniMapReduceCluster
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test a map/reduce against a multi-region table    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testMultiRegionTable
parameter_list|()
throws|throws
name|IOException
block|{
name|runTestOnTable
argument_list|(
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|MULTI_REGION_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCombiner
parameter_list|()
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// force use of combiner for testing purposes
name|conf
operator|.
name|setInt
argument_list|(
literal|"mapreduce.map.combine.minspills"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|runTestOnTable
argument_list|(
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|MULTI_REGION_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Implements mapper logic for use across APIs.    */
specifier|protected
specifier|static
name|Put
name|map
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|Result
name|value
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|value
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"There should only be one input column"
argument_list|)
throw|;
block|}
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|>
name|cf
init|=
name|value
operator|.
name|getMap
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|cf
operator|.
name|containsKey
argument_list|(
name|INPUT_FAMILY
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Wrong input columns. Missing: '"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|INPUT_FAMILY
argument_list|)
operator|+
literal|"'."
argument_list|)
throw|;
block|}
comment|// Get the original value and reverse it
name|String
name|originalValue
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|value
operator|.
name|getValue
argument_list|(
name|INPUT_FAMILY
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|StringBuilder
name|newValue
init|=
operator|new
name|StringBuilder
argument_list|(
name|originalValue
argument_list|)
decl_stmt|;
name|newValue
operator|.
name|reverse
argument_list|()
expr_stmt|;
comment|// Now set the value to be collected
name|Put
name|outval
init|=
operator|new
name|Put
argument_list|(
name|key
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|outval
operator|.
name|add
argument_list|(
name|OUTPUT_FAMILY
argument_list|,
literal|null
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newValue
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|outval
return|;
block|}
specifier|protected
name|void
name|verify
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|Table
name|table
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|boolean
name|verified
init|=
literal|false
decl_stmt|;
name|long
name|pause
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|5
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|int
name|numRetries
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|5
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numRetries
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|getLog
argument_list|()
operator|.
name|info
argument_list|(
literal|"Verification attempt #"
operator|+
name|i
argument_list|)
expr_stmt|;
name|verifyAttempt
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|verified
operator|=
literal|true
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// If here, a cell was empty. Presume its because updates came in
comment|// after the scanner had been opened. Wait a while and retry.
name|getLog
argument_list|()
operator|.
name|debug
argument_list|(
literal|"Verification attempt failed: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|pause
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
name|assertTrue
argument_list|(
name|verified
argument_list|)
expr_stmt|;
block|}
comment|/**    * Looks at every value of the mapreduce output and verifies that indeed    * the values have been reversed.    * @param table Table to scan.    * @throws IOException    * @throws NullPointerException if we failed to find a cell value    */
specifier|private
name|void
name|verifyAttempt
parameter_list|(
specifier|final
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
throws|,
name|NullPointerException
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|TableInputFormat
operator|.
name|addColumns
argument_list|(
name|scan
argument_list|,
name|columns
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
try|try
block|{
name|Iterator
argument_list|<
name|Result
argument_list|>
name|itr
init|=
name|scanner
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|itr
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
while|while
condition|(
name|itr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Result
name|r
init|=
name|itr
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|getLog
argument_list|()
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|r
operator|.
name|size
argument_list|()
operator|>
literal|2
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Too many results, expected 2 got "
operator|+
name|r
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|byte
index|[]
name|firstValue
init|=
literal|null
decl_stmt|;
name|byte
index|[]
name|secondValue
init|=
literal|null
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|r
operator|.
name|listCells
argument_list|()
control|)
block|{
if|if
condition|(
name|count
operator|==
literal|0
condition|)
block|{
name|firstValue
operator|=
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|count
operator|==
literal|1
condition|)
block|{
name|secondValue
operator|=
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
if|if
condition|(
name|count
operator|==
literal|2
condition|)
block|{
break|break;
block|}
block|}
if|if
condition|(
name|firstValue
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|": first value is null"
argument_list|)
throw|;
block|}
name|String
name|first
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|firstValue
argument_list|)
decl_stmt|;
if|if
condition|(
name|secondValue
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|": second value is null"
argument_list|)
throw|;
block|}
name|byte
index|[]
name|secondReversed
init|=
operator|new
name|byte
index|[
name|secondValue
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|j
init|=
name|secondValue
operator|.
name|length
operator|-
literal|1
init|;
name|j
operator|>=
literal|0
condition|;
name|j
operator|--
operator|,
name|i
operator|++
control|)
block|{
name|secondReversed
index|[
name|i
index|]
operator|=
name|secondValue
index|[
name|j
index|]
expr_stmt|;
block|}
name|String
name|second
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|secondReversed
argument_list|)
decl_stmt|;
if|if
condition|(
name|first
operator|.
name|compareTo
argument_list|(
name|second
argument_list|)
operator|!=
literal|0
condition|)
block|{
if|if
condition|(
name|getLog
argument_list|()
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|getLog
argument_list|()
operator|.
name|debug
argument_list|(
literal|"second key is not the reverse of first. row="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|", first value="
operator|+
name|first
operator|+
literal|", second value="
operator|+
name|second
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

