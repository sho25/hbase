begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2006 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|io
operator|.
name|Text
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_comment
comment|/**  * Client used evaluating HBase performance and scalability.  Steps through  * one of a set of hardcoded tests or 'experiments' (e.g. a random reads test,  * a random writes test, etc.). Pass on the command-line which test to run,  * how many clients are participating in this experiment, and the row range  * this client instance is to operate on. Run  *<code>java EvaluationClient --help</code> to obtain usage.  *   *<p>This class implements the client used in the  *<i>Performance Evaluation</i> benchmarks described in Section 7 of the<a  * href="http://labs.google.com/papers/bigtable.html">Bigtable</a>  * paper on pages 8-10.  */
end_comment

begin_class
specifier|public
class|class
name|EvaluationClient
implements|implements
name|HConstants
block|{
specifier|private
specifier|final
name|Logger
name|LOG
init|=
name|Logger
operator|.
name|getLogger
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
specifier|private
specifier|static
specifier|final
name|int
name|ROW_LENGTH
init|=
literal|1024
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ONE_HUNDRED_MB
init|=
literal|1024
operator|*
literal|1024
operator|*
literal|1
comment|/*100 RESTORE*/
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ROWS_PER_100_MB
init|=
name|ONE_HUNDRED_MB
operator|/
name|ROW_LENGTH
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ONE_GB
init|=
name|ONE_HUNDRED_MB
operator|*
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ROWS_PER_GB
init|=
name|ONE_GB
operator|/
name|ROW_LENGTH
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|COLUMN_NAME
init|=
operator|new
name|Text
argument_list|(
name|COLUMN_FAMILY
operator|+
literal|"data"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HTableDescriptor
name|tableDescriptor
decl_stmt|;
static|static
block|{
name|tableDescriptor
operator|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"TestTable"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|.
name|addFamily
argument_list|(
name|COLUMN_FAMILY
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
enum|enum
name|Test
block|{
name|RANDOM_READ
block|,
name|RANDOM_READ_MEM
block|,
name|RANDOM_WRITE
block|,
name|SEQUENTIAL_READ
block|,
name|SEQUENTIAL_WRITE
block|,
name|SCAN
block|}
empty_stmt|;
specifier|private
name|Random
name|rand
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|HClient
name|client
decl_stmt|;
specifier|private
name|boolean
name|miniCluster
decl_stmt|;
specifier|private
name|int
name|N
decl_stmt|;
comment|// Number of clients and HRegionServers
specifier|private
name|int
name|range
decl_stmt|;
comment|// Row range for this client
specifier|private
name|int
name|R
decl_stmt|;
comment|// Total number of rows
specifier|private
name|EvaluationClient
parameter_list|()
block|{
name|this
operator|.
name|rand
operator|=
operator|new
name|Random
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
operator|new
name|HBaseConfiguration
argument_list|()
expr_stmt|;
name|this
operator|.
name|miniCluster
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|client
operator|=
operator|new
name|HClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|N
operator|=
literal|1
expr_stmt|;
comment|// Default is one client
name|this
operator|.
name|range
operator|=
literal|0
expr_stmt|;
comment|// Range for this client
name|this
operator|.
name|R
operator|=
name|ROWS_PER_GB
expr_stmt|;
comment|// Default for one client
block|}
specifier|private
name|byte
index|[]
name|generateValue
parameter_list|()
block|{
name|StringBuilder
name|val
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
while|while
condition|(
name|val
operator|.
name|length
argument_list|()
operator|<
name|ROW_LENGTH
condition|)
block|{
name|val
operator|.
name|append
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|rand
operator|.
name|nextLong
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|val
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|()
return|;
block|}
specifier|private
name|long
name|randomRead
parameter_list|(
name|int
name|startRow
parameter_list|,
name|int
name|nRows
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"startRow: "
operator|+
name|startRow
operator|+
literal|", nRows: "
operator|+
name|nRows
argument_list|)
expr_stmt|;
name|client
operator|.
name|openTable
argument_list|(
name|tableDescriptor
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|int
name|lastRow
init|=
name|startRow
operator|+
name|nRows
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|startRow
init|;
name|i
operator|<
name|lastRow
condition|;
name|i
operator|++
control|)
block|{
name|client
operator|.
name|get
argument_list|(
operator|new
name|Text
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|()
operator|%
name|R
argument_list|)
argument_list|)
argument_list|,
name|COLUMN_NAME
argument_list|)
expr_stmt|;
block|}
return|return
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
return|;
block|}
specifier|private
name|long
name|randomWrite
parameter_list|(
name|int
name|startRow
parameter_list|,
name|int
name|nRows
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"startRow: "
operator|+
name|startRow
operator|+
literal|", nRows: "
operator|+
name|nRows
argument_list|)
expr_stmt|;
name|client
operator|.
name|openTable
argument_list|(
name|tableDescriptor
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|int
name|lastRow
init|=
name|startRow
operator|+
name|nRows
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|startRow
init|;
name|i
operator|<
name|lastRow
condition|;
name|i
operator|++
control|)
block|{
name|long
name|lockid
init|=
name|client
operator|.
name|startUpdate
argument_list|(
operator|new
name|Text
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|()
operator|%
name|R
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|client
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|COLUMN_NAME
argument_list|,
name|generateValue
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|commit
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
block|}
return|return
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
return|;
block|}
specifier|private
name|long
name|scan
parameter_list|(
name|int
name|startRow
parameter_list|,
name|int
name|nRows
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"startRow: "
operator|+
name|startRow
operator|+
literal|", nRows: "
operator|+
name|nRows
argument_list|)
expr_stmt|;
name|client
operator|.
name|openTable
argument_list|(
name|tableDescriptor
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|HScannerInterface
name|scanner
init|=
name|client
operator|.
name|obtainScanner
argument_list|(
operator|new
name|Text
index|[]
block|{
name|COLUMN_NAME
block|}
argument_list|,
operator|new
name|Text
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|startRow
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
name|int
name|lastRow
init|=
name|startRow
operator|+
name|nRows
decl_stmt|;
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|startRow
init|;
name|i
operator|<
name|lastRow
condition|;
name|i
operator|++
control|)
block|{
name|scanner
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
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
return|return
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
return|;
block|}
specifier|private
name|long
name|sequentialRead
parameter_list|(
name|int
name|startRow
parameter_list|,
name|int
name|nRows
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"startRow: "
operator|+
name|startRow
operator|+
literal|", nRows: "
operator|+
name|nRows
argument_list|)
expr_stmt|;
name|client
operator|.
name|openTable
argument_list|(
name|tableDescriptor
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|int
name|lastRow
init|=
name|startRow
operator|+
name|nRows
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|startRow
init|;
name|i
operator|<
name|lastRow
condition|;
name|i
operator|++
control|)
block|{
name|client
operator|.
name|get
argument_list|(
operator|new
name|Text
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|,
name|COLUMN_NAME
argument_list|)
expr_stmt|;
block|}
return|return
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
return|;
block|}
specifier|private
name|long
name|sequentialWrite
parameter_list|(
name|int
name|startRow
parameter_list|,
name|int
name|nRows
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"startRow: "
operator|+
name|startRow
operator|+
literal|", nRows: "
operator|+
name|nRows
argument_list|)
expr_stmt|;
name|client
operator|.
name|openTable
argument_list|(
name|tableDescriptor
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|int
name|lastRow
init|=
name|startRow
operator|+
name|nRows
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|startRow
init|;
name|i
operator|<
name|lastRow
condition|;
name|i
operator|++
control|)
block|{
name|long
name|lockid
init|=
name|client
operator|.
name|startUpdate
argument_list|(
operator|new
name|Text
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|client
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|COLUMN_NAME
argument_list|,
name|generateValue
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|commit
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
block|}
return|return
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
return|;
block|}
specifier|private
name|void
name|runNIsOne
parameter_list|(
name|Test
name|test
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|client
operator|.
name|createTable
argument_list|(
name|tableDescriptor
argument_list|)
expr_stmt|;
name|long
name|totalElapsedTime
init|=
literal|0
decl_stmt|;
name|int
name|nRows
init|=
name|R
operator|/
operator|(
literal|10
operator|*
name|N
operator|)
decl_stmt|;
if|if
condition|(
name|test
operator|==
name|Test
operator|.
name|RANDOM_READ
operator|||
name|test
operator|==
name|Test
operator|.
name|RANDOM_READ_MEM
operator|||
name|test
operator|==
name|Test
operator|.
name|SCAN
operator|||
name|test
operator|==
name|Test
operator|.
name|SEQUENTIAL_READ
operator|||
name|test
operator|==
name|Test
operator|.
name|SEQUENTIAL_WRITE
condition|)
block|{
for|for
control|(
name|int
name|range
init|=
literal|0
init|;
name|range
operator|<
literal|10
condition|;
name|range
operator|++
control|)
block|{
name|long
name|elapsedTime
init|=
name|sequentialWrite
argument_list|(
name|range
operator|*
name|nRows
argument_list|,
name|nRows
argument_list|)
decl_stmt|;
if|if
condition|(
name|test
operator|==
name|Test
operator|.
name|SEQUENTIAL_WRITE
condition|)
block|{
name|totalElapsedTime
operator|+=
name|elapsedTime
expr_stmt|;
block|}
block|}
block|}
switch|switch
condition|(
name|test
condition|)
block|{
case|case
name|RANDOM_READ
case|:
for|for
control|(
name|int
name|range
init|=
literal|0
init|;
name|range
operator|<
literal|10
condition|;
name|range
operator|++
control|)
block|{
name|long
name|elapsedTime
init|=
name|randomRead
argument_list|(
name|range
operator|*
name|nRows
argument_list|,
name|nRows
argument_list|)
decl_stmt|;
name|totalElapsedTime
operator|+=
name|elapsedTime
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"Random read of "
operator|+
name|R
operator|+
literal|" rows completed in: "
argument_list|)
expr_stmt|;
break|break;
case|case
name|RANDOM_READ_MEM
case|:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not yet implemented"
argument_list|)
throw|;
case|case
name|RANDOM_WRITE
case|:
for|for
control|(
name|int
name|range
init|=
literal|0
init|;
name|range
operator|<
literal|10
condition|;
name|range
operator|++
control|)
block|{
name|long
name|elapsedTime
init|=
name|randomWrite
argument_list|(
name|range
operator|*
name|nRows
argument_list|,
name|nRows
argument_list|)
decl_stmt|;
name|totalElapsedTime
operator|+=
name|elapsedTime
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"Random write of "
operator|+
name|R
operator|+
literal|" rows completed in: "
argument_list|)
expr_stmt|;
break|break;
case|case
name|SCAN
case|:
for|for
control|(
name|int
name|range
init|=
literal|0
init|;
name|range
operator|<
literal|10
condition|;
name|range
operator|++
control|)
block|{
name|long
name|elapsedTime
init|=
name|scan
argument_list|(
name|range
operator|*
name|nRows
argument_list|,
name|nRows
argument_list|)
decl_stmt|;
name|totalElapsedTime
operator|+=
name|elapsedTime
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"Scan of "
operator|+
name|R
operator|+
literal|" rows completed in: "
argument_list|)
expr_stmt|;
break|break;
case|case
name|SEQUENTIAL_READ
case|:
for|for
control|(
name|int
name|range
init|=
literal|0
init|;
name|range
operator|<
literal|10
condition|;
name|range
operator|++
control|)
block|{
name|long
name|elapsedTime
init|=
name|sequentialRead
argument_list|(
name|range
operator|*
name|nRows
argument_list|,
name|nRows
argument_list|)
decl_stmt|;
name|totalElapsedTime
operator|+=
name|elapsedTime
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"Sequential read of "
operator|+
name|R
operator|+
literal|" rows completed in: "
argument_list|)
expr_stmt|;
break|break;
case|case
name|SEQUENTIAL_WRITE
case|:
comment|// We already did it!
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"Sequential write of "
operator|+
name|R
operator|+
literal|" rows completed in: "
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid command value: "
operator|+
name|test
argument_list|)
throw|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
operator|(
name|totalElapsedTime
operator|/
literal|1000.0
operator|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|client
operator|.
name|deleteTable
argument_list|(
name|tableDescriptor
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|runOneTest
parameter_list|(
name|Test
name|cmd
parameter_list|)
block|{   }
specifier|private
name|void
name|runTest
parameter_list|(
name|Test
name|test
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|test
operator|==
name|Test
operator|.
name|RANDOM_READ_MEM
condition|)
block|{
comment|// For this one test, so all fits in memory, make R smaller (See
comment|// pg. 9 of BigTable paper).
name|R
operator|=
name|ROWS_PER_100_MB
operator|*
name|N
expr_stmt|;
block|}
name|MiniHBaseCluster
name|hbaseMiniCluster
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|miniCluster
condition|)
block|{
name|hbaseMiniCluster
operator|=
operator|new
name|MiniHBaseCluster
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|N
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|N
operator|==
literal|1
condition|)
block|{
comment|// If there is only one client and one HRegionServer, we assume nothing
comment|// has been set up at all.
name|runNIsOne
argument_list|(
name|test
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Else, run
name|runOneTest
argument_list|(
name|test
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|this
operator|.
name|miniCluster
operator|&&
name|hbaseMiniCluster
operator|!=
literal|null
condition|)
block|{
name|hbaseMiniCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|printUsage
parameter_list|()
block|{
name|printUsage
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|printUsage
parameter_list|(
specifier|final
name|String
name|message
parameter_list|)
block|{
if|if
condition|(
name|message
operator|!=
literal|null
operator|&&
name|message
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Usage: java "
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"[--master=host:port] [--miniCluster]<command><args>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Options:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" master          Specify host and port of HBase "
operator|+
literal|"cluster master. If not present,"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"                 address is read from configuration"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" miniCluster     Run the test on an HBaseMiniCluster"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Commands:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" randomRead      Run random read test"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" randomReadMem   Run random read test where table "
operator|+
literal|"is in memory"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" randomWrite     Run random write test"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" sequentialRead  Run sequential read test"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" sequentialWrite Run sequential write test"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" scan            Run scan test"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Args:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" nclients        Integer. Required. Total number of "
operator|+
literal|"clients (and HRegionServers)"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"                 running: 1<= value<= 500"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" range           Integer. Required. 0<= value<= "
operator|+
literal|"(nclients * 10) - 1"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|getArgs
parameter_list|(
specifier|final
name|int
name|start
parameter_list|,
specifier|final
name|String
index|[]
name|args
parameter_list|)
block|{
if|if
condition|(
name|start
operator|+
literal|1
operator|>
name|args
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"must supply the number of clients "
operator|+
literal|"and the range for this client."
argument_list|)
throw|;
block|}
name|N
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
name|start
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|N
operator|>
literal|500
operator|||
name|N
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Number of clients must be between "
operator|+
literal|"1 and 500."
argument_list|)
throw|;
block|}
name|R
operator|=
name|ROWS_PER_GB
operator|*
name|N
expr_stmt|;
name|range
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
name|start
operator|+
literal|1
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|range
argument_list|<
literal|0
operator|||
name|range
argument_list|>
argument_list|(
name|N
operator|*
literal|10
argument_list|)
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Range must be between 0 and "
operator|+
operator|(
operator|(
name|N
operator|*
literal|10
operator|)
operator|-
literal|1
operator|)
argument_list|)
throw|;
block|}
block|}
specifier|private
name|int
name|doCommandLine
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
block|{
comment|// Process command-line args. TODO: Better cmd-line processing
comment|// (but hopefully something not as painful as cli options).
name|int
name|errCode
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|1
condition|)
block|{
name|printUsage
argument_list|()
expr_stmt|;
return|return
name|errCode
return|;
block|}
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|args
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|cmd
init|=
name|args
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-h"
argument_list|)
operator|||
name|cmd
operator|.
name|startsWith
argument_list|(
literal|"--h"
argument_list|)
condition|)
block|{
name|printUsage
argument_list|()
expr_stmt|;
name|errCode
operator|=
literal|0
expr_stmt|;
break|break;
block|}
specifier|final
name|String
name|masterArgKey
init|=
literal|"--master="
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|masterArgKey
argument_list|)
condition|)
block|{
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|MASTER_ADDRESS
argument_list|,
name|cmd
operator|.
name|substring
argument_list|(
name|masterArgKey
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|String
name|miniClusterArgKey
init|=
literal|"--miniCluster"
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|miniClusterArgKey
argument_list|)
condition|)
block|{
name|this
operator|.
name|miniCluster
operator|=
literal|true
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"randomRead"
argument_list|)
condition|)
block|{
name|getArgs
argument_list|(
name|i
operator|+
literal|1
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|Test
operator|.
name|RANDOM_READ
argument_list|)
expr_stmt|;
name|errCode
operator|=
literal|0
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"randomReadMem"
argument_list|)
condition|)
block|{
name|getArgs
argument_list|(
name|i
operator|+
literal|1
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|Test
operator|.
name|RANDOM_READ_MEM
argument_list|)
expr_stmt|;
name|errCode
operator|=
literal|0
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"randomWrite"
argument_list|)
condition|)
block|{
name|getArgs
argument_list|(
name|i
operator|+
literal|1
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|Test
operator|.
name|RANDOM_WRITE
argument_list|)
expr_stmt|;
name|errCode
operator|=
literal|0
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"sequentialRead"
argument_list|)
condition|)
block|{
name|getArgs
argument_list|(
name|i
operator|+
literal|1
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|Test
operator|.
name|SEQUENTIAL_READ
argument_list|)
expr_stmt|;
name|errCode
operator|=
literal|0
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"sequentialWrite"
argument_list|)
condition|)
block|{
name|getArgs
argument_list|(
name|i
operator|+
literal|1
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|Test
operator|.
name|SEQUENTIAL_WRITE
argument_list|)
expr_stmt|;
name|errCode
operator|=
literal|0
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"scan"
argument_list|)
condition|)
block|{
name|getArgs
argument_list|(
name|i
operator|+
literal|1
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|Test
operator|.
name|SCAN
argument_list|)
expr_stmt|;
name|errCode
operator|=
literal|0
expr_stmt|;
break|break;
block|}
name|printUsage
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
return|return
name|errCode
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
block|{
name|StaticTestEnvironment
operator|.
name|initialize
argument_list|()
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|new
name|EvaluationClient
argument_list|()
operator|.
name|doCommandLine
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

