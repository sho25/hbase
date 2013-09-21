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
name|util
package|;
end_package

begin_import
import|import static
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
name|test
operator|.
name|LoadTestDataGenerator
operator|.
name|INCREMENT
import|;
end_import

begin_import
import|import static
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
name|test
operator|.
name|LoadTestDataGenerator
operator|.
name|MUTATE_INFO
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
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
name|TableName
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
name|Tag
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
name|util
operator|.
name|test
operator|.
name|LoadTestDataGenerator
import|;
end_import

begin_comment
comment|/** Creates multiple threads that write key/values into the */
end_comment

begin_class
specifier|public
class|class
name|MultiThreadedWriter
extends|extends
name|MultiThreadedWriterBase
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MultiThreadedWriter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|HBaseWriterThread
argument_list|>
name|writers
init|=
operator|new
name|HashSet
argument_list|<
name|HBaseWriterThread
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|isMultiPut
init|=
literal|false
decl_stmt|;
specifier|private
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
comment|// TODO: Make this configurable
specifier|private
name|int
name|minTagLength
init|=
literal|16
decl_stmt|;
specifier|private
name|int
name|maxTagLength
init|=
literal|512
decl_stmt|;
specifier|public
name|MultiThreadedWriter
parameter_list|(
name|LoadTestDataGenerator
name|dataGen
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|TableName
name|tableName
parameter_list|)
block|{
name|super
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|tableName
argument_list|,
literal|"W"
argument_list|)
expr_stmt|;
block|}
comment|/** Use multi-puts vs. separate puts for every column in a row */
specifier|public
name|void
name|setMultiPut
parameter_list|(
name|boolean
name|isMultiPut
parameter_list|)
block|{
name|this
operator|.
name|isMultiPut
operator|=
name|isMultiPut
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|long
name|startKey
parameter_list|,
name|long
name|endKey
parameter_list|,
name|int
name|numThreads
parameter_list|,
name|boolean
name|useTags
parameter_list|,
name|int
name|minNumTags
parameter_list|,
name|int
name|maxNumTags
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|start
argument_list|(
name|startKey
argument_list|,
name|endKey
argument_list|,
name|numThreads
argument_list|,
name|useTags
argument_list|,
name|minNumTags
argument_list|,
name|maxNumTags
argument_list|)
expr_stmt|;
if|if
condition|(
name|verbose
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Inserting keys ["
operator|+
name|startKey
operator|+
literal|", "
operator|+
name|endKey
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numThreads
condition|;
operator|++
name|i
control|)
block|{
name|HBaseWriterThread
name|writer
init|=
operator|new
name|HBaseWriterThread
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|writers
operator|.
name|add
argument_list|(
name|writer
argument_list|)
expr_stmt|;
block|}
name|startThreads
argument_list|(
name|writers
argument_list|)
expr_stmt|;
block|}
specifier|private
class|class
name|HBaseWriterThread
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|HTable
name|table
decl_stmt|;
specifier|public
name|HBaseWriterThread
parameter_list|(
name|int
name|writerId
parameter_list|)
throws|throws
name|IOException
block|{
name|setName
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"_"
operator|+
name|writerId
argument_list|)
expr_stmt|;
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|long
name|rowKeyBase
decl_stmt|;
name|byte
index|[]
index|[]
name|columnFamilies
init|=
name|dataGenerator
operator|.
name|getColumnFamilies
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|rowKeyBase
operator|=
name|nextKeyToWrite
operator|.
name|getAndIncrement
argument_list|()
operator|)
operator|<
name|endKey
condition|)
block|{
name|byte
index|[]
name|rowKey
init|=
name|dataGenerator
operator|.
name|getDeterministicUniqueKey
argument_list|(
name|rowKeyBase
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|numKeys
operator|.
name|addAndGet
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|int
name|columnCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|cf
range|:
name|columnFamilies
control|)
block|{
name|byte
index|[]
index|[]
name|columns
init|=
name|dataGenerator
operator|.
name|generateColumnsForCf
argument_list|(
name|rowKey
argument_list|,
name|cf
argument_list|)
decl_stmt|;
name|int
name|numTags
decl_stmt|;
if|if
condition|(
name|minNumTags
operator|==
name|maxNumTags
condition|)
block|{
name|numTags
operator|=
name|minNumTags
expr_stmt|;
block|}
else|else
block|{
name|numTags
operator|=
name|minNumTags
operator|+
name|random
operator|.
name|nextInt
argument_list|(
name|maxNumTags
operator|-
name|minNumTags
argument_list|)
expr_stmt|;
block|}
name|Tag
index|[]
name|tags
init|=
operator|new
name|Tag
index|[
name|numTags
index|]
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|column
range|:
name|columns
control|)
block|{
name|byte
index|[]
name|value
init|=
name|dataGenerator
operator|.
name|generateValue
argument_list|(
name|rowKey
argument_list|,
name|cf
argument_list|,
name|column
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tag
init|=
name|LoadTestTool
operator|.
name|generateData
argument_list|(
name|random
argument_list|,
name|minTagLength
operator|+
name|random
operator|.
name|nextInt
argument_list|(
name|maxTagLength
operator|-
name|minTagLength
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|useTags
condition|)
block|{
for|for
control|(
name|int
name|n
init|=
literal|0
init|;
name|n
operator|<
name|numTags
condition|;
name|n
operator|++
control|)
block|{
name|Tag
name|t
init|=
operator|new
name|Tag
argument_list|(
operator|(
name|byte
operator|)
name|n
argument_list|,
name|tag
argument_list|)
decl_stmt|;
name|tags
index|[
name|n
index|]
operator|=
name|t
expr_stmt|;
block|}
name|put
operator|.
name|add
argument_list|(
name|cf
argument_list|,
name|column
argument_list|,
name|value
argument_list|,
name|tags
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|put
operator|.
name|add
argument_list|(
name|cf
argument_list|,
name|column
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
operator|++
name|columnCount
expr_stmt|;
if|if
condition|(
operator|!
name|isMultiPut
condition|)
block|{
name|insert
argument_list|(
name|table
argument_list|,
name|put
argument_list|,
name|rowKeyBase
argument_list|)
expr_stmt|;
name|numCols
operator|.
name|addAndGet
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|rowKey
argument_list|)
expr_stmt|;
block|}
block|}
name|long
name|rowKeyHash
init|=
name|Arrays
operator|.
name|hashCode
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|cf
argument_list|,
name|MUTATE_INFO
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|cf
argument_list|,
name|INCREMENT
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowKeyHash
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isMultiPut
condition|)
block|{
name|insert
argument_list|(
name|table
argument_list|,
name|put
argument_list|,
name|rowKeyBase
argument_list|)
expr_stmt|;
name|numCols
operator|.
name|addAndGet
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|rowKey
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|isMultiPut
condition|)
block|{
if|if
condition|(
name|verbose
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Preparing put for key = ["
operator|+
name|rowKey
operator|+
literal|"], "
operator|+
name|columnCount
operator|+
literal|" columns"
argument_list|)
expr_stmt|;
block|}
name|insert
argument_list|(
name|table
argument_list|,
name|put
argument_list|,
name|rowKeyBase
argument_list|)
expr_stmt|;
name|numCols
operator|.
name|addAndGet
argument_list|(
name|columnCount
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|trackWroteKeys
condition|)
block|{
name|wroteKeys
operator|.
name|add
argument_list|(
name|rowKeyBase
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
try|try
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error closing table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|numThreadsWorking
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|waitForFinish
parameter_list|()
block|{
name|super
operator|.
name|waitForFinish
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Failed to write keys: "
operator|+
name|failedKeySet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Long
name|key
range|:
name|failedKeySet
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Failed to write key: "
operator|+
name|key
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

