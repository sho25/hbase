begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Map
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
name|Configured
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
name|KeyValue
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
name|mapred
operator|.
name|FileOutputFormat
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
name|JobClient
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
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|lib
operator|.
name|IdentityReducer
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
name|util
operator|.
name|Tool
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
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_comment
comment|/**  * A job with a map to count rows.  * Map outputs table rows IF the input row has columns that have content.  * Uses an {@link IdentityReducer}  */
end_comment

begin_class
annotation|@
name|Deprecated
specifier|public
class|class
name|RowCounter
extends|extends
name|Configured
implements|implements
name|Tool
block|{
comment|// Name of this 'program'
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"rowcounter"
decl_stmt|;
comment|/**    * Mapper that runs the count.    */
specifier|static
class|class
name|RowCounterMapper
implements|implements
name|TableMap
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
block|{
specifier|private
specifier|static
enum|enum
name|Counters
block|{
name|ROWS
block|}
specifier|public
name|void
name|map
parameter_list|(
name|ImmutableBytesWritable
name|row
parameter_list|,
name|Result
name|values
parameter_list|,
name|OutputCollector
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
name|output
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|content
init|=
literal|false
decl_stmt|;
for|for
control|(
name|KeyValue
name|value
range|:
name|values
operator|.
name|list
argument_list|()
control|)
block|{
if|if
condition|(
name|value
operator|.
name|getValue
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|content
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|content
condition|)
block|{
comment|// Don't count rows that are all empty values.
return|return;
block|}
comment|// Give out same value every time.  We're only interested in the row/key
name|reporter
operator|.
name|incrCounter
argument_list|(
name|Counters
operator|.
name|ROWS
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|jc
parameter_list|)
block|{
comment|// Nothing to do.
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Nothing to do.
block|}
block|}
comment|/**    * @param args    * @return the JobConf    * @throws IOException    */
specifier|public
name|JobConf
name|createSubmittableJob
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|JobConf
name|c
init|=
operator|new
name|JobConf
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|c
operator|.
name|setJobName
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
comment|// Columns are space delimited
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
specifier|final
name|int
name|columnoffset
init|=
literal|2
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|columnoffset
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
if|if
condition|(
name|i
operator|>
name|columnoffset
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
comment|// Second argument is the table name.
name|TableMapReduceUtil
operator|.
name|initTableMapJob
argument_list|(
name|args
index|[
literal|1
index|]
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|,
name|RowCounterMapper
operator|.
name|class
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|,
name|Result
operator|.
name|class
argument_list|,
name|c
argument_list|)
expr_stmt|;
name|c
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// First arg is the output directory.
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|c
argument_list|,
operator|new
name|Path
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|c
return|;
block|}
specifier|static
name|int
name|printUsage
parameter_list|()
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|NAME
operator|+
literal|"<outputdir><tablename><column1> [<column2>...]"
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
specifier|public
name|int
name|run
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Make sure there are at least 3 parameters
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|3
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"ERROR: Wrong number of parameters: "
operator|+
name|args
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|printUsage
argument_list|()
return|;
block|}
name|JobClient
operator|.
name|runJob
argument_list|(
name|createSubmittableJob
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
comment|/**    * @param args    * @throws Exception    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|HBaseConfiguration
name|c
init|=
operator|new
name|HBaseConfiguration
argument_list|()
decl_stmt|;
name|int
name|errCode
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|c
argument_list|,
operator|new
name|RowCounter
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|errCode
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

