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
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
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
name|Map
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
name|lang3
operator|.
name|RandomUtils
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
name|client
operator|.
name|Append
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
name|Delete
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
name|Get
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
name|Increment
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
name|Mutation
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
name|RetriesExhaustedWithDetailsException
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|MutationProto
operator|.
name|MutationType
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
name|StringUtils
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
name|Preconditions
import|;
end_import

begin_comment
comment|/** Creates multiple threads that write key/values into the */
end_comment

begin_class
specifier|public
class|class
name|MultiThreadedUpdater
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
name|MultiThreadedUpdater
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|Set
argument_list|<
name|HBaseUpdaterThread
argument_list|>
name|updaters
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|MultiThreadedWriterBase
name|writer
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|isBatchUpdate
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|ignoreNonceConflicts
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|double
name|updatePercent
decl_stmt|;
specifier|public
name|MultiThreadedUpdater
parameter_list|(
name|LoadTestDataGenerator
name|dataGen
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|double
name|updatePercent
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|tableName
argument_list|,
literal|"U"
argument_list|)
expr_stmt|;
name|this
operator|.
name|updatePercent
operator|=
name|updatePercent
expr_stmt|;
block|}
comment|/** Use batch vs. separate updates for every column in a row */
specifier|public
name|void
name|setBatchUpdate
parameter_list|(
name|boolean
name|isBatchUpdate
parameter_list|)
block|{
name|this
operator|.
name|isBatchUpdate
operator|=
name|isBatchUpdate
expr_stmt|;
block|}
specifier|public
name|void
name|linkToWriter
parameter_list|(
name|MultiThreadedWriterBase
name|writer
parameter_list|)
block|{
name|this
operator|.
name|writer
operator|=
name|writer
expr_stmt|;
name|writer
operator|.
name|setTrackWroteKeys
argument_list|(
literal|true
argument_list|)
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
literal|"Updating keys ["
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
name|addUpdaterThreads
argument_list|(
name|numThreads
argument_list|)
expr_stmt|;
name|startThreads
argument_list|(
name|updaters
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|addUpdaterThreads
parameter_list|(
name|int
name|numThreads
parameter_list|)
throws|throws
name|IOException
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
name|numThreads
condition|;
operator|++
name|i
control|)
block|{
name|HBaseUpdaterThread
name|updater
init|=
operator|new
name|HBaseUpdaterThread
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|updaters
operator|.
name|add
argument_list|(
name|updater
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|long
name|getNextKeyToUpdate
parameter_list|()
block|{
if|if
condition|(
name|writer
operator|==
literal|null
condition|)
block|{
return|return
name|nextKeyToWrite
operator|.
name|getAndIncrement
argument_list|()
return|;
block|}
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|nextKeyToWrite
operator|.
name|get
argument_list|()
operator|>=
name|endKey
condition|)
block|{
comment|// Finished the whole key range
return|return
name|endKey
return|;
block|}
while|while
condition|(
name|nextKeyToWrite
operator|.
name|get
argument_list|()
operator|>
name|writer
operator|.
name|wroteUpToKey
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|long
name|k
init|=
name|nextKeyToWrite
operator|.
name|getAndIncrement
argument_list|()
decl_stmt|;
if|if
condition|(
name|writer
operator|.
name|failedToWriteKey
argument_list|(
name|k
argument_list|)
condition|)
block|{
name|failedKeySet
operator|.
name|add
argument_list|(
name|k
argument_list|)
expr_stmt|;
return|return
name|getNextKeyToUpdate
argument_list|()
return|;
block|}
return|return
name|k
return|;
block|}
block|}
specifier|protected
class|class
name|HBaseUpdaterThread
extends|extends
name|Thread
block|{
specifier|protected
specifier|final
name|Table
name|table
decl_stmt|;
specifier|public
name|HBaseUpdaterThread
parameter_list|(
name|int
name|updaterId
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
name|updaterId
argument_list|)
expr_stmt|;
name|table
operator|=
name|createTable
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|Table
name|createTable
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|()
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
name|getNextKeyToUpdate
argument_list|()
operator|)
operator|<
name|endKey
condition|)
block|{
if|if
condition|(
name|RandomUtils
operator|.
name|nextInt
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
operator|<
name|updatePercent
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
name|Increment
name|inc
init|=
operator|new
name|Increment
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|Append
name|app
init|=
operator|new
name|Append
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
name|long
name|cfHash
init|=
name|Arrays
operator|.
name|hashCode
argument_list|(
name|cf
argument_list|)
decl_stmt|;
name|inc
operator|.
name|addColumn
argument_list|(
name|cf
argument_list|,
name|INCREMENT
argument_list|,
name|cfHash
argument_list|)
expr_stmt|;
name|buf
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// Clear the buffer
name|buf
operator|.
name|append
argument_list|(
literal|"#"
argument_list|)
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|INCREMENT
argument_list|)
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
operator|.
name|append
argument_list|(
name|MutationType
operator|.
name|INCREMENT
operator|.
name|getNumber
argument_list|()
argument_list|)
expr_stmt|;
name|app
operator|.
name|addColumn
argument_list|(
name|cf
argument_list|,
name|MUTATE_INFO
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|buf
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
operator|++
name|columnCount
expr_stmt|;
if|if
condition|(
operator|!
name|isBatchUpdate
condition|)
block|{
name|mutate
argument_list|(
name|table
argument_list|,
name|inc
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
name|inc
operator|=
operator|new
name|Increment
argument_list|(
name|rowKey
argument_list|)
expr_stmt|;
name|mutate
argument_list|(
name|table
argument_list|,
name|app
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
name|app
operator|=
operator|new
name|Append
argument_list|(
name|rowKey
argument_list|)
expr_stmt|;
block|}
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|get
operator|.
name|addFamily
argument_list|(
name|cf
argument_list|)
expr_stmt|;
try|try
block|{
name|get
operator|=
name|dataGenerator
operator|.
name|beforeGet
argument_list|(
name|rowKeyBase
argument_list|,
name|get
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Ideally wont happen
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to modify the get from the load generator  = ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|get
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|"], column family = ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|cf
argument_list|)
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|Result
name|result
init|=
name|getRow
argument_list|(
name|get
argument_list|,
name|rowKeyBase
argument_list|,
name|cf
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|columnValues
init|=
name|result
operator|!=
literal|null
condition|?
name|result
operator|.
name|getFamilyMap
argument_list|(
name|cf
argument_list|)
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|columnValues
operator|==
literal|null
condition|)
block|{
name|int
name|specialPermCellInsertionFactor
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|dataGenerator
operator|.
name|getArgs
argument_list|()
index|[
literal|2
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
operator|(
name|int
operator|)
name|rowKeyBase
operator|%
name|specialPermCellInsertionFactor
operator|==
literal|0
operator|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Null result expected for the rowkey "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|rowKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|failedKeySet
operator|.
name|add
argument_list|(
name|rowKeyBase
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to update the row with key = ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|rowKey
argument_list|)
operator|+
literal|"], since we could not get the original row"
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|columnValues
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|byte
index|[]
name|column
range|:
name|columnValues
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|column
argument_list|,
name|INCREMENT
argument_list|)
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|column
argument_list|,
name|MUTATE_INFO
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|MutationType
name|mt
init|=
name|MutationType
operator|.
name|valueOf
argument_list|(
name|RandomUtils
operator|.
name|nextInt
argument_list|(
literal|0
argument_list|,
name|MutationType
operator|.
name|values
argument_list|()
operator|.
name|length
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|columnHash
init|=
name|Arrays
operator|.
name|hashCode
argument_list|(
name|column
argument_list|)
decl_stmt|;
name|long
name|hashCode
init|=
name|cfHash
operator|+
name|columnHash
decl_stmt|;
name|byte
index|[]
name|hashCodeBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|hashCode
argument_list|)
decl_stmt|;
name|byte
index|[]
name|checkedValue
init|=
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
decl_stmt|;
if|if
condition|(
name|hashCode
operator|%
literal|2
operator|==
literal|0
condition|)
block|{
name|Cell
name|kv
init|=
name|result
operator|.
name|getColumnLatestCell
argument_list|(
name|cf
argument_list|,
name|column
argument_list|)
decl_stmt|;
name|checkedValue
operator|=
name|kv
operator|!=
literal|null
condition|?
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|kv
argument_list|)
else|:
literal|null
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|checkedValue
argument_list|,
literal|"Column value to be checked should not be null"
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// Clear the buffer
name|buf
operator|.
name|append
argument_list|(
literal|"#"
argument_list|)
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|column
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
operator|++
name|columnCount
expr_stmt|;
switch|switch
condition|(
name|mt
condition|)
block|{
case|case
name|PUT
case|:
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|cf
argument_list|,
name|column
argument_list|,
name|hashCodeBytes
argument_list|)
expr_stmt|;
name|mutate
argument_list|(
name|table
argument_list|,
name|put
argument_list|,
name|rowKeyBase
argument_list|,
name|rowKey
argument_list|,
name|cf
argument_list|,
name|column
argument_list|,
name|checkedValue
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|MutationType
operator|.
name|PUT
operator|.
name|getNumber
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE
case|:
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
comment|// Delete all versions since a put
comment|// could be called multiple times if CM is used
name|delete
operator|.
name|addColumns
argument_list|(
name|cf
argument_list|,
name|column
argument_list|)
expr_stmt|;
name|mutate
argument_list|(
name|table
argument_list|,
name|delete
argument_list|,
name|rowKeyBase
argument_list|,
name|rowKey
argument_list|,
name|cf
argument_list|,
name|column
argument_list|,
name|checkedValue
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|MutationType
operator|.
name|DELETE
operator|.
name|getNumber
argument_list|()
argument_list|)
expr_stmt|;
break|break;
default|default:
name|buf
operator|.
name|append
argument_list|(
name|MutationType
operator|.
name|APPEND
operator|.
name|getNumber
argument_list|()
argument_list|)
expr_stmt|;
name|app
operator|.
name|addColumn
argument_list|(
name|cf
argument_list|,
name|column
argument_list|,
name|hashCodeBytes
argument_list|)
expr_stmt|;
block|}
name|app
operator|.
name|addColumn
argument_list|(
name|cf
argument_list|,
name|MUTATE_INFO
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|buf
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isBatchUpdate
condition|)
block|{
name|mutate
argument_list|(
name|table
argument_list|,
name|app
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
name|app
operator|=
operator|new
name|Append
argument_list|(
name|rowKey
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|isBatchUpdate
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
literal|"Preparing increment and append for key = ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|rowKey
argument_list|)
operator|+
literal|"], "
operator|+
name|columnCount
operator|+
literal|" columns"
argument_list|)
expr_stmt|;
block|}
name|mutate
argument_list|(
name|table
argument_list|,
name|inc
argument_list|,
name|rowKeyBase
argument_list|)
expr_stmt|;
name|mutate
argument_list|(
name|table
argument_list|,
name|app
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
name|closeHTable
argument_list|()
expr_stmt|;
name|numThreadsWorking
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|closeHTable
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
block|}
specifier|protected
name|Result
name|getRow
parameter_list|(
name|Get
name|get
parameter_list|,
name|long
name|rowKeyBase
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|)
block|{
name|Result
name|result
init|=
literal|null
decl_stmt|;
try|try
block|{
name|result
operator|=
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ie
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to get the row for key = ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|get
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|"], column family = ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|cf
argument_list|)
operator|+
literal|"]"
argument_list|,
name|ie
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|void
name|mutate
parameter_list|(
name|Table
name|table
parameter_list|,
name|Mutation
name|m
parameter_list|,
name|long
name|keyBase
parameter_list|)
block|{
name|mutate
argument_list|(
name|table
argument_list|,
name|m
argument_list|,
name|keyBase
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|mutate
parameter_list|(
name|Table
name|table
parameter_list|,
name|Mutation
name|m
parameter_list|,
name|long
name|keyBase
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|,
name|byte
index|[]
name|q
parameter_list|,
name|byte
index|[]
name|v
parameter_list|)
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
name|m
operator|=
name|dataGenerator
operator|.
name|beforeMutate
argument_list|(
name|keyBase
argument_list|,
name|m
argument_list|)
expr_stmt|;
if|if
condition|(
name|m
operator|instanceof
name|Increment
condition|)
block|{
name|table
operator|.
name|increment
argument_list|(
operator|(
name|Increment
operator|)
name|m
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|m
operator|instanceof
name|Append
condition|)
block|{
name|table
operator|.
name|append
argument_list|(
operator|(
name|Append
operator|)
name|m
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|m
operator|instanceof
name|Put
condition|)
block|{
name|table
operator|.
name|checkAndPut
argument_list|(
name|row
argument_list|,
name|cf
argument_list|,
name|q
argument_list|,
name|v
argument_list|,
operator|(
name|Put
operator|)
name|m
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|m
operator|instanceof
name|Delete
condition|)
block|{
name|table
operator|.
name|checkAndDelete
argument_list|(
name|row
argument_list|,
name|cf
argument_list|,
name|q
argument_list|,
name|v
argument_list|,
operator|(
name|Delete
operator|)
name|m
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unsupported mutation "
operator|+
name|m
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
throw|;
block|}
name|totalOpTimeMs
operator|.
name|addAndGet
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|ignoreNonceConflicts
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Detected nonce conflict, ignoring: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|totalOpTimeMs
operator|.
name|addAndGet
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
argument_list|)
expr_stmt|;
return|return;
block|}
name|failedKeySet
operator|.
name|add
argument_list|(
name|keyBase
argument_list|)
expr_stmt|;
name|String
name|exceptionInfo
decl_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|RetriesExhaustedWithDetailsException
condition|)
block|{
name|RetriesExhaustedWithDetailsException
name|aggEx
init|=
operator|(
name|RetriesExhaustedWithDetailsException
operator|)
name|e
decl_stmt|;
name|exceptionInfo
operator|=
name|aggEx
operator|.
name|getExhaustiveDescription
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|exceptionInfo
operator|=
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to mutate: "
operator|+
name|keyBase
operator|+
literal|" after "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|)
operator|+
literal|"ms; region information: "
operator|+
name|getRegionDebugInfoSafe
argument_list|(
name|table
argument_list|,
name|m
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|"; errors: "
operator|+
name|exceptionInfo
argument_list|)
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
literal|"Failed to update keys: "
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
literal|"Failed to update key: "
operator|+
name|key
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|mutate
parameter_list|(
name|Table
name|table
parameter_list|,
name|Mutation
name|m
parameter_list|,
name|long
name|keyBase
parameter_list|)
block|{
name|mutate
argument_list|(
name|table
argument_list|,
name|m
argument_list|,
name|keyBase
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|mutate
parameter_list|(
name|Table
name|table
parameter_list|,
name|Mutation
name|m
parameter_list|,
name|long
name|keyBase
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|,
name|byte
index|[]
name|q
parameter_list|,
name|byte
index|[]
name|v
parameter_list|)
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
name|m
operator|=
name|dataGenerator
operator|.
name|beforeMutate
argument_list|(
name|keyBase
argument_list|,
name|m
argument_list|)
expr_stmt|;
if|if
condition|(
name|m
operator|instanceof
name|Increment
condition|)
block|{
name|table
operator|.
name|increment
argument_list|(
operator|(
name|Increment
operator|)
name|m
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|m
operator|instanceof
name|Append
condition|)
block|{
name|table
operator|.
name|append
argument_list|(
operator|(
name|Append
operator|)
name|m
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|m
operator|instanceof
name|Put
condition|)
block|{
name|table
operator|.
name|checkAndPut
argument_list|(
name|row
argument_list|,
name|cf
argument_list|,
name|q
argument_list|,
name|v
argument_list|,
operator|(
name|Put
operator|)
name|m
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|m
operator|instanceof
name|Delete
condition|)
block|{
name|table
operator|.
name|checkAndDelete
argument_list|(
name|row
argument_list|,
name|cf
argument_list|,
name|q
argument_list|,
name|v
argument_list|,
operator|(
name|Delete
operator|)
name|m
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unsupported mutation "
operator|+
name|m
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
throw|;
block|}
name|totalOpTimeMs
operator|.
name|addAndGet
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|failedKeySet
operator|.
name|add
argument_list|(
name|keyBase
argument_list|)
expr_stmt|;
name|String
name|exceptionInfo
decl_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|RetriesExhaustedWithDetailsException
condition|)
block|{
name|RetriesExhaustedWithDetailsException
name|aggEx
init|=
operator|(
name|RetriesExhaustedWithDetailsException
operator|)
name|e
decl_stmt|;
name|exceptionInfo
operator|=
name|aggEx
operator|.
name|getExhaustiveDescription
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|StringWriter
name|stackWriter
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|PrintWriter
name|pw
init|=
operator|new
name|PrintWriter
argument_list|(
name|stackWriter
argument_list|)
decl_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|(
name|pw
argument_list|)
expr_stmt|;
name|pw
operator|.
name|flush
argument_list|()
expr_stmt|;
name|exceptionInfo
operator|=
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to mutate: "
operator|+
name|keyBase
operator|+
literal|" after "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|)
operator|+
literal|"ms; region information: "
operator|+
name|getRegionDebugInfoSafe
argument_list|(
name|table
argument_list|,
name|m
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|"; errors: "
operator|+
name|exceptionInfo
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|setIgnoreNonceConflicts
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|this
operator|.
name|ignoreNonceConflicts
operator|=
name|value
expr_stmt|;
block|}
block|}
end_class

end_unit

