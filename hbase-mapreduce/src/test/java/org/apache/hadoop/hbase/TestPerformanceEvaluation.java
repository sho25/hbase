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
name|io
operator|.
name|BufferedReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
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
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NoSuchElementException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
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
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|fs
operator|.
name|FSDataInputStream
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|PerformanceEvaluation
operator|.
name|RandomReadTest
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
name|PerformanceEvaluation
operator|.
name|TestOptions
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
name|codehaus
operator|.
name|jackson
operator|.
name|JsonGenerationException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|JsonMappingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ObjectMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
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

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Histogram
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Snapshot
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|UniformReservoir
import|;
end_import

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
name|TestPerformanceEvaluation
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|HTU
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|JsonGenerationException
throws|,
name|JsonMappingException
throws|,
name|IOException
block|{
name|PerformanceEvaluation
operator|.
name|TestOptions
name|options
init|=
operator|new
name|PerformanceEvaluation
operator|.
name|TestOptions
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
operator|!
name|options
operator|.
name|isAutoFlush
argument_list|()
argument_list|)
expr_stmt|;
name|options
operator|.
name|setAutoFlush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ObjectMapper
name|mapper
init|=
operator|new
name|ObjectMapper
argument_list|()
decl_stmt|;
name|String
name|optionsString
init|=
name|mapper
operator|.
name|writeValueAsString
argument_list|(
name|options
argument_list|)
decl_stmt|;
name|PerformanceEvaluation
operator|.
name|TestOptions
name|optionsDeserialized
init|=
name|mapper
operator|.
name|readValue
argument_list|(
name|optionsString
argument_list|,
name|PerformanceEvaluation
operator|.
name|TestOptions
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|optionsDeserialized
operator|.
name|isAutoFlush
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Exercise the mr spec writing.  Simple assertions to make sure it is basically working.    * @throws IOException    */
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testWriteInputFile
parameter_list|()
throws|throws
name|IOException
block|{
name|TestOptions
name|opts
init|=
operator|new
name|PerformanceEvaluation
operator|.
name|TestOptions
argument_list|()
decl_stmt|;
specifier|final
name|int
name|clients
init|=
literal|10
decl_stmt|;
name|opts
operator|.
name|setNumClientThreads
argument_list|(
name|clients
argument_list|)
expr_stmt|;
name|opts
operator|.
name|setPerClientRunRows
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|Path
name|dir
init|=
name|PerformanceEvaluation
operator|.
name|writeInputFile
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|opts
argument_list|,
name|HTU
operator|.
name|getDataTestDir
argument_list|()
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
name|PerformanceEvaluation
operator|.
name|JOB_INPUT_FILENAME
argument_list|)
decl_stmt|;
name|long
name|len
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|p
argument_list|)
operator|.
name|getLen
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|len
operator|>
literal|0
argument_list|)
expr_stmt|;
name|byte
index|[]
name|content
init|=
operator|new
name|byte
index|[
operator|(
name|int
operator|)
name|len
index|]
decl_stmt|;
name|FSDataInputStream
name|dis
init|=
name|fs
operator|.
name|open
argument_list|(
name|p
argument_list|)
decl_stmt|;
try|try
block|{
name|dis
operator|.
name|readFully
argument_list|(
name|content
argument_list|)
expr_stmt|;
name|BufferedReader
name|br
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|content
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|br
operator|.
name|readLine
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|clients
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|dis
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSizeCalculation
parameter_list|()
block|{
name|TestOptions
name|opts
init|=
operator|new
name|PerformanceEvaluation
operator|.
name|TestOptions
argument_list|()
decl_stmt|;
name|opts
operator|=
name|PerformanceEvaluation
operator|.
name|calculateRowsAndSize
argument_list|(
name|opts
argument_list|)
expr_stmt|;
name|int
name|rows
init|=
name|opts
operator|.
name|getPerClientRunRows
argument_list|()
decl_stmt|;
comment|// Default row count
specifier|final
name|int
name|defaultPerClientRunRows
init|=
literal|1024
operator|*
literal|1024
decl_stmt|;
name|assertEquals
argument_list|(
name|defaultPerClientRunRows
argument_list|,
name|rows
argument_list|)
expr_stmt|;
comment|// If size is 2G, then twice the row count.
name|opts
operator|.
name|setSize
argument_list|(
literal|2.0f
argument_list|)
expr_stmt|;
name|opts
operator|=
name|PerformanceEvaluation
operator|.
name|calculateRowsAndSize
argument_list|(
name|opts
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|defaultPerClientRunRows
operator|*
literal|2
argument_list|,
name|opts
operator|.
name|getPerClientRunRows
argument_list|()
argument_list|)
expr_stmt|;
comment|// If two clients, then they get half the rows each.
name|opts
operator|.
name|setNumClientThreads
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|opts
operator|=
name|PerformanceEvaluation
operator|.
name|calculateRowsAndSize
argument_list|(
name|opts
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|defaultPerClientRunRows
argument_list|,
name|opts
operator|.
name|getPerClientRunRows
argument_list|()
argument_list|)
expr_stmt|;
comment|// What if valueSize is 'random'? Then half of the valueSize so twice the rows.
name|opts
operator|.
name|valueRandom
operator|=
literal|true
expr_stmt|;
name|opts
operator|=
name|PerformanceEvaluation
operator|.
name|calculateRowsAndSize
argument_list|(
name|opts
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|defaultPerClientRunRows
operator|*
literal|2
argument_list|,
name|opts
operator|.
name|getPerClientRunRows
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRandomReadCalculation
parameter_list|()
block|{
name|TestOptions
name|opts
init|=
operator|new
name|PerformanceEvaluation
operator|.
name|TestOptions
argument_list|()
decl_stmt|;
name|opts
operator|=
name|PerformanceEvaluation
operator|.
name|calculateRowsAndSize
argument_list|(
name|opts
argument_list|)
expr_stmt|;
name|int
name|rows
init|=
name|opts
operator|.
name|getPerClientRunRows
argument_list|()
decl_stmt|;
comment|// Default row count
specifier|final
name|int
name|defaultPerClientRunRows
init|=
literal|1024
operator|*
literal|1024
decl_stmt|;
name|assertEquals
argument_list|(
name|defaultPerClientRunRows
argument_list|,
name|rows
argument_list|)
expr_stmt|;
comment|// If size is 2G, then twice the row count.
name|opts
operator|.
name|setSize
argument_list|(
literal|2.0f
argument_list|)
expr_stmt|;
name|opts
operator|.
name|setPerClientRunRows
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|opts
operator|.
name|setCmdName
argument_list|(
name|PerformanceEvaluation
operator|.
name|RANDOM_READ
argument_list|)
expr_stmt|;
name|opts
operator|=
name|PerformanceEvaluation
operator|.
name|calculateRowsAndSize
argument_list|(
name|opts
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|opts
operator|.
name|getPerClientRunRows
argument_list|()
argument_list|)
expr_stmt|;
comment|// If two clients, then they get half the rows each.
name|opts
operator|.
name|setNumClientThreads
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|opts
operator|=
name|PerformanceEvaluation
operator|.
name|calculateRowsAndSize
argument_list|(
name|opts
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|opts
operator|.
name|getPerClientRunRows
argument_list|()
argument_list|)
expr_stmt|;
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
comment|// assuming we will get one before this loop expires
name|boolean
name|foundValue
init|=
literal|false
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
literal|10000000
condition|;
name|i
operator|++
control|)
block|{
name|int
name|randomRow
init|=
name|PerformanceEvaluation
operator|.
name|generateRandomRow
argument_list|(
name|random
argument_list|,
name|opts
operator|.
name|totalRows
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomRow
operator|>
literal|1000
condition|)
block|{
name|foundValue
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
literal|"We need to get a value more than 1000"
argument_list|,
name|foundValue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testZipfian
parameter_list|()
throws|throws
name|NoSuchMethodException
throws|,
name|SecurityException
throws|,
name|InstantiationException
throws|,
name|IllegalAccessException
throws|,
name|IllegalArgumentException
throws|,
name|InvocationTargetException
block|{
name|TestOptions
name|opts
init|=
operator|new
name|PerformanceEvaluation
operator|.
name|TestOptions
argument_list|()
decl_stmt|;
name|opts
operator|.
name|setValueZipf
argument_list|(
literal|true
argument_list|)
expr_stmt|;
specifier|final
name|int
name|valueSize
init|=
literal|1024
decl_stmt|;
name|opts
operator|.
name|setValueSize
argument_list|(
name|valueSize
argument_list|)
expr_stmt|;
name|RandomReadTest
name|rrt
init|=
operator|new
name|RandomReadTest
argument_list|(
literal|null
argument_list|,
name|opts
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Constructor
argument_list|<
name|?
argument_list|>
name|ctor
init|=
name|Histogram
operator|.
name|class
operator|.
name|getDeclaredConstructor
argument_list|(
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Reservoir
operator|.
name|class
argument_list|)
decl_stmt|;
name|ctor
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Histogram
name|histogram
init|=
operator|(
name|Histogram
operator|)
name|ctor
operator|.
name|newInstance
argument_list|(
operator|new
name|UniformReservoir
argument_list|(
literal|1024
operator|*
literal|500
argument_list|)
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|histogram
operator|.
name|update
argument_list|(
name|rrt
operator|.
name|getValueLength
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Snapshot
name|snapshot
init|=
name|histogram
operator|.
name|getSnapshot
argument_list|()
decl_stmt|;
name|double
name|stddev
init|=
name|snapshot
operator|.
name|getStdDev
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|stddev
operator|!=
literal|0
operator|&&
name|stddev
operator|!=
literal|1.0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|snapshot
operator|.
name|getStdDev
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|double
name|median
init|=
name|snapshot
operator|.
name|getMedian
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|median
operator|!=
literal|0
operator|&&
name|median
operator|!=
literal|1
operator|&&
name|median
operator|!=
name|valueSize
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseOptsWithThreads
parameter_list|()
block|{
name|Queue
argument_list|<
name|String
argument_list|>
name|opts
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|cmdName
init|=
literal|"sequentialWrite"
decl_stmt|;
name|int
name|threads
init|=
literal|1
decl_stmt|;
name|opts
operator|.
name|offer
argument_list|(
name|cmdName
argument_list|)
expr_stmt|;
name|opts
operator|.
name|offer
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|threads
argument_list|)
argument_list|)
expr_stmt|;
name|PerformanceEvaluation
operator|.
name|TestOptions
name|options
init|=
name|PerformanceEvaluation
operator|.
name|parseOpts
argument_list|(
name|opts
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|options
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|options
operator|.
name|getCmdName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|cmdName
argument_list|,
name|options
operator|.
name|getCmdName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|threads
argument_list|,
name|options
operator|.
name|getNumClientThreads
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseOptsWrongThreads
parameter_list|()
block|{
name|Queue
argument_list|<
name|String
argument_list|>
name|opts
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|cmdName
init|=
literal|"sequentialWrite"
decl_stmt|;
name|opts
operator|.
name|offer
argument_list|(
name|cmdName
argument_list|)
expr_stmt|;
name|opts
operator|.
name|offer
argument_list|(
literal|"qq"
argument_list|)
expr_stmt|;
try|try
block|{
name|PerformanceEvaluation
operator|.
name|parseOpts
argument_list|(
name|opts
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Command "
operator|+
name|cmdName
operator|+
literal|" does not have threads number"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|NumberFormatException
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseOptsNoThreads
parameter_list|()
block|{
name|Queue
argument_list|<
name|String
argument_list|>
name|opts
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|cmdName
init|=
literal|"sequentialWrite"
decl_stmt|;
try|try
block|{
name|PerformanceEvaluation
operator|.
name|parseOpts
argument_list|(
name|opts
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Command "
operator|+
name|cmdName
operator|+
literal|" does not have threads number"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|NoSuchElementException
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
