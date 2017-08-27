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
name|mapred
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyInt
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|times
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|PrintStream
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
name|HBaseConfiguration
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
name|MapReduceTests
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
name|mapred
operator|.
name|RowCounter
operator|.
name|RowCounterMapper
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
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|OutputCollector
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
name|mapred
operator|.
name|Reporter
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
name|org
operator|.
name|mockito
operator|.
name|Mockito
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MapReduceTests
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
name|TestRowCounter
block|{
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|void
name|shouldPrintUsage
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|expectedOutput
init|=
literal|"rowcounter<outputdir><tablename><column1> [<column2>...]"
decl_stmt|;
name|String
name|result
init|=
operator|new
name|OutputReader
argument_list|(
name|System
operator|.
name|out
argument_list|)
block|{
annotation|@
name|Override
name|void
name|doRead
parameter_list|()
block|{
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|RowCounter
operator|.
name|printUsage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
operator|.
name|read
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|startsWith
argument_list|(
name|expectedOutput
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|void
name|shouldExitAndPrintUsageSinceParameterNumberLessThanThree
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"one"
block|,
literal|"two"
block|}
decl_stmt|;
name|String
name|line
init|=
literal|"ERROR: Wrong number of parameters: "
operator|+
name|args
operator|.
name|length
decl_stmt|;
name|String
name|result
init|=
operator|new
name|OutputReader
argument_list|(
name|System
operator|.
name|err
argument_list|)
block|{
annotation|@
name|Override
name|void
name|doRead
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
operator|new
name|RowCounter
argument_list|()
operator|.
name|run
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
operator|.
name|read
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|startsWith
argument_list|(
name|line
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"deprecation"
block|,
literal|"unchecked"
block|}
argument_list|)
specifier|public
name|void
name|shouldRegInReportEveryIncomingRow
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|iterationNumber
init|=
literal|999
decl_stmt|;
name|RowCounter
operator|.
name|RowCounterMapper
name|mapper
init|=
operator|new
name|RowCounter
operator|.
name|RowCounterMapper
argument_list|()
decl_stmt|;
name|Reporter
name|reporter
init|=
name|mock
argument_list|(
name|Reporter
operator|.
name|class
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
name|iterationNumber
condition|;
name|i
operator|++
control|)
name|mapper
operator|.
name|map
argument_list|(
name|mock
argument_list|(
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
argument_list|,
name|mock
argument_list|(
name|Result
operator|.
name|class
argument_list|)
argument_list|,
name|mock
argument_list|(
name|OutputCollector
operator|.
name|class
argument_list|)
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|reporter
argument_list|,
name|times
argument_list|(
name|iterationNumber
argument_list|)
argument_list|)
operator|.
name|incrCounter
argument_list|(
name|any
argument_list|(
name|Enum
operator|.
name|class
argument_list|)
argument_list|,
name|anyInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"deprecation"
block|}
argument_list|)
specifier|public
name|void
name|shouldCreateAndRunSubmittableJob
parameter_list|()
throws|throws
name|Exception
block|{
name|RowCounter
name|rCounter
init|=
operator|new
name|RowCounter
argument_list|()
decl_stmt|;
name|rCounter
operator|.
name|setConf
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"\temp"
block|,
literal|"tableA"
block|,
literal|"column1"
block|,
literal|"column2"
block|,
literal|"column3"
block|}
decl_stmt|;
name|JobConf
name|jobConfig
init|=
name|rCounter
operator|.
name|createSubmittableJob
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|jobConfig
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|jobConfig
operator|.
name|getNumReduceTasks
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"rowcounter"
argument_list|,
name|jobConfig
operator|.
name|getJobName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|jobConfig
operator|.
name|getMapOutputValueClass
argument_list|()
argument_list|,
name|Result
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|jobConfig
operator|.
name|getMapperClass
argument_list|()
argument_list|,
name|RowCounterMapper
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|jobConfig
operator|.
name|get
argument_list|(
name|TableInputFormat
operator|.
name|COLUMN_LIST
argument_list|)
argument_list|,
name|Joiner
operator|.
name|on
argument_list|(
literal|' '
argument_list|)
operator|.
name|join
argument_list|(
literal|"column1"
argument_list|,
literal|"column2"
argument_list|,
literal|"column3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|jobConfig
operator|.
name|getMapOutputKeyClass
argument_list|()
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
enum|enum
name|Outs
block|{
name|OUT
block|,
name|ERR
block|}
specifier|private
specifier|static
specifier|abstract
class|class
name|OutputReader
block|{
specifier|private
specifier|final
name|PrintStream
name|ps
decl_stmt|;
specifier|private
name|PrintStream
name|oldPrintStream
decl_stmt|;
specifier|private
name|Outs
name|outs
decl_stmt|;
specifier|protected
name|OutputReader
parameter_list|(
name|PrintStream
name|ps
parameter_list|)
block|{
name|this
operator|.
name|ps
operator|=
name|ps
expr_stmt|;
block|}
specifier|protected
name|String
name|read
parameter_list|()
throws|throws
name|Exception
block|{
name|ByteArrayOutputStream
name|outBytes
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
if|if
condition|(
name|ps
operator|==
name|System
operator|.
name|out
condition|)
block|{
name|oldPrintStream
operator|=
name|System
operator|.
name|out
expr_stmt|;
name|outs
operator|=
name|Outs
operator|.
name|OUT
expr_stmt|;
name|System
operator|.
name|setOut
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|outBytes
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ps
operator|==
name|System
operator|.
name|err
condition|)
block|{
name|oldPrintStream
operator|=
name|System
operator|.
name|err
expr_stmt|;
name|outs
operator|=
name|Outs
operator|.
name|ERR
expr_stmt|;
name|System
operator|.
name|setErr
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|outBytes
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"OutputReader: unsupported PrintStream"
argument_list|)
throw|;
block|}
try|try
block|{
name|doRead
argument_list|()
expr_stmt|;
return|return
operator|new
name|String
argument_list|(
name|outBytes
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
finally|finally
block|{
switch|switch
condition|(
name|outs
condition|)
block|{
case|case
name|OUT
case|:
block|{
name|System
operator|.
name|setOut
argument_list|(
name|oldPrintStream
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|ERR
case|:
block|{
name|System
operator|.
name|setErr
argument_list|(
name|oldPrintStream
argument_list|)
expr_stmt|;
break|break;
block|}
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"OutputReader: unsupported PrintStream"
argument_list|)
throw|;
block|}
block|}
block|}
specifier|abstract
name|void
name|doRead
parameter_list|()
throws|throws
name|Exception
function_decl|;
block|}
block|}
end_class

end_unit
