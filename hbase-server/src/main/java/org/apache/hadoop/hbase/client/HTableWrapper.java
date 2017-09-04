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
name|client
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|MethodDescriptor
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Service
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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
name|ArrayList
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
name|concurrent
operator|.
name|ExecutorService
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
name|CompareOperator
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
name|HBaseInterfaceAudience
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
name|HTableDescriptor
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|coprocessor
operator|.
name|Batch
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
name|coprocessor
operator|.
name|Batch
operator|.
name|Callback
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
name|coprocessor
operator|.
name|BaseEnvironment
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
name|ipc
operator|.
name|CoprocessorRpcChannel
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
name|MultipleIOException
import|;
end_import

begin_comment
comment|/**  * A wrapper for HTable. Can be used to restrict privilege.  *  * Currently it just helps to track tables opened by a Coprocessor and  * facilitate close of them if it is aborted.  *  * We also disallow row locking.  *  * There is nothing now that will stop a coprocessor from using HTable  * objects directly instead of this API, but in the future we intend to  * analyze coprocessor implementations as they are loaded and reject those  * which attempt to use objects and methods outside the Environment  * sandbox.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
specifier|final
class|class
name|HTableWrapper
implements|implements
name|Table
block|{
specifier|private
specifier|final
name|Table
name|table
decl_stmt|;
specifier|private
specifier|final
name|ClusterConnection
name|connection
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Table
argument_list|>
name|openTables
decl_stmt|;
comment|/**    * @param openTables External list of tables used for tracking wrappers.    * @throws IOException    */
specifier|public
specifier|static
name|Table
name|createWrapper
parameter_list|(
name|List
argument_list|<
name|Table
argument_list|>
name|openTables
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|BaseEnvironment
name|env
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|HTableWrapper
argument_list|(
name|openTables
argument_list|,
name|tableName
argument_list|,
name|CoprocessorHConnection
operator|.
name|getConnectionForEnvironment
argument_list|(
name|env
argument_list|)
argument_list|,
name|pool
argument_list|)
return|;
block|}
specifier|private
name|HTableWrapper
parameter_list|(
name|List
argument_list|<
name|Table
argument_list|>
name|openTables
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|ClusterConnection
name|connection
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|,
name|pool
argument_list|)
expr_stmt|;
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
name|this
operator|.
name|openTables
operator|=
name|openTables
expr_stmt|;
name|this
operator|.
name|openTables
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|internalClose
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|IOException
argument_list|>
name|exceptions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
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
name|exceptions
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
comment|// have to self-manage our connection, as per the HTable contract
if|if
condition|(
name|this
operator|.
name|connection
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|connection
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
name|exceptions
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|exceptions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
name|MultipleIOException
operator|.
name|createIOException
argument_list|(
name|exceptions
argument_list|)
throw|;
block|}
block|}
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|table
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|internalClose
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|openTables
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Result
name|get
parameter_list|(
name|Get
name|get
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|exists
parameter_list|(
name|Get
name|get
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|exists
argument_list|(
name|get
argument_list|)
return|;
block|}
specifier|public
name|boolean
index|[]
name|existsAll
parameter_list|(
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|existsAll
argument_list|(
name|gets
argument_list|)
return|;
block|}
comment|/**    * @deprecated Use {@link #existsAll(java.util.List)}  instead. since 2.0.  remove in 3.0    */
annotation|@
name|Deprecated
specifier|public
name|Boolean
index|[]
name|exists
parameter_list|(
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Do convertion.
name|boolean
index|[]
name|exists
init|=
name|table
operator|.
name|existsAll
argument_list|(
name|gets
argument_list|)
decl_stmt|;
if|if
condition|(
name|exists
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Boolean
index|[]
name|results
init|=
operator|new
name|Boolean
index|[
name|exists
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
init|;
name|i
operator|<
name|exists
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|results
index|[
name|i
index|]
operator|=
name|exists
index|[
name|i
index|]
condition|?
name|Boolean
operator|.
name|TRUE
else|:
name|Boolean
operator|.
name|FALSE
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
specifier|public
name|void
name|put
parameter_list|(
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|put
parameter_list|(
name|List
argument_list|<
name|Put
argument_list|>
name|puts
parameter_list|)
throws|throws
name|IOException
block|{
name|table
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|delete
parameter_list|(
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
block|{
name|table
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|delete
parameter_list|(
name|List
argument_list|<
name|Delete
argument_list|>
name|deletes
parameter_list|)
throws|throws
name|IOException
block|{
name|table
operator|.
name|delete
argument_list|(
name|deletes
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|checkAndPut
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|checkAndPut
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|,
name|put
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|checkAndPut
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOp
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|checkAndPut
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|compareOp
argument_list|,
name|value
argument_list|,
name|put
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|checkAndPut
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOperator
name|op
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|checkAndPut
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|op
argument_list|,
name|value
argument_list|,
name|put
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|checkAndDelete
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|checkAndDelete
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|value
argument_list|,
name|delete
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|checkAndDelete
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOp
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|checkAndDelete
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|compareOp
argument_list|,
name|value
argument_list|,
name|delete
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|checkAndDelete
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOperator
name|op
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|checkAndDelete
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|op
argument_list|,
name|value
argument_list|,
name|delete
argument_list|)
return|;
block|}
specifier|public
name|long
name|incrementColumnValue
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|amount
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|incrementColumnValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|amount
argument_list|)
return|;
block|}
specifier|public
name|long
name|incrementColumnValue
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|amount
parameter_list|,
name|Durability
name|durability
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|incrementColumnValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|amount
argument_list|,
name|durability
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|append
parameter_list|(
name|Append
name|append
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|append
argument_list|(
name|append
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|increment
parameter_list|(
name|Increment
name|increment
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|increment
argument_list|(
name|increment
argument_list|)
return|;
block|}
specifier|public
name|ResultScanner
name|getScanner
parameter_list|(
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
return|;
block|}
specifier|public
name|ResultScanner
name|getScanner
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|getScanner
argument_list|(
name|family
argument_list|)
return|;
block|}
specifier|public
name|ResultScanner
name|getScanner
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|getScanner
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
return|;
block|}
specifier|public
name|HTableDescriptor
name|getTableDescriptor
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|getTableDescriptor
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableDescriptor
name|getDescriptor
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|getDescriptor
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableName
name|getName
parameter_list|()
block|{
return|return
name|table
operator|.
name|getName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|batch
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|,
name|Object
index|[]
name|results
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|table
operator|.
name|batch
argument_list|(
name|actions
argument_list|,
name|results
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|R
parameter_list|>
name|void
name|batchCallback
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|actions
parameter_list|,
name|Object
index|[]
name|results
parameter_list|,
name|Batch
operator|.
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|table
operator|.
name|batchCallback
argument_list|(
name|actions
argument_list|,
name|results
argument_list|,
name|callback
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Result
index|[]
name|get
parameter_list|(
name|List
argument_list|<
name|Get
argument_list|>
name|gets
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|get
argument_list|(
name|gets
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CoprocessorRpcChannel
name|coprocessorService
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
block|{
return|return
name|table
operator|.
name|coprocessorService
argument_list|(
name|row
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
extends|extends
name|Service
parameter_list|,
name|R
parameter_list|>
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|R
argument_list|>
name|coprocessorService
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|service
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|Batch
operator|.
name|Call
argument_list|<
name|T
argument_list|,
name|R
argument_list|>
name|callable
parameter_list|)
throws|throws
name|ServiceException
throws|,
name|Throwable
block|{
return|return
name|table
operator|.
name|coprocessorService
argument_list|(
name|service
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|callable
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
extends|extends
name|Service
parameter_list|,
name|R
parameter_list|>
name|void
name|coprocessorService
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|service
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|Batch
operator|.
name|Call
argument_list|<
name|T
argument_list|,
name|R
argument_list|>
name|callable
parameter_list|,
name|Batch
operator|.
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|ServiceException
throws|,
name|Throwable
block|{
name|table
operator|.
name|coprocessorService
argument_list|(
name|service
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|callable
argument_list|,
name|callback
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|mutateRow
parameter_list|(
name|RowMutations
name|rm
parameter_list|)
throws|throws
name|IOException
block|{
name|table
operator|.
name|mutateRow
argument_list|(
name|rm
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|R
extends|extends
name|Message
parameter_list|>
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|R
argument_list|>
name|batchCoprocessorService
parameter_list|(
name|MethodDescriptor
name|methodDescriptor
parameter_list|,
name|Message
name|request
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|R
name|responsePrototype
parameter_list|)
throws|throws
name|ServiceException
throws|,
name|Throwable
block|{
return|return
name|table
operator|.
name|batchCoprocessorService
argument_list|(
name|methodDescriptor
argument_list|,
name|request
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|responsePrototype
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|R
extends|extends
name|Message
parameter_list|>
name|void
name|batchCoprocessorService
parameter_list|(
name|MethodDescriptor
name|methodDescriptor
parameter_list|,
name|Message
name|request
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|R
name|responsePrototype
parameter_list|,
name|Callback
argument_list|<
name|R
argument_list|>
name|callback
parameter_list|)
throws|throws
name|ServiceException
throws|,
name|Throwable
block|{
name|table
operator|.
name|batchCoprocessorService
argument_list|(
name|methodDescriptor
argument_list|,
name|request
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|responsePrototype
argument_list|,
name|callback
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndMutate
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOp
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|RowMutations
name|rm
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|checkAndMutate
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|compareOp
argument_list|,
name|value
argument_list|,
name|rm
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkAndMutate
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|CompareOperator
name|op
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|RowMutations
name|rm
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|table
operator|.
name|checkAndMutate
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|op
argument_list|,
name|value
argument_list|,
name|rm
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setOperationTimeout
parameter_list|(
name|int
name|operationTimeout
parameter_list|)
block|{
name|table
operator|.
name|setOperationTimeout
argument_list|(
name|operationTimeout
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getOperationTimeout
parameter_list|()
block|{
return|return
name|table
operator|.
name|getOperationTimeout
argument_list|()
return|;
block|}
annotation|@
name|Override
annotation|@
name|Deprecated
specifier|public
name|void
name|setRpcTimeout
parameter_list|(
name|int
name|rpcTimeout
parameter_list|)
block|{
name|table
operator|.
name|setRpcTimeout
argument_list|(
name|rpcTimeout
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setWriteRpcTimeout
parameter_list|(
name|int
name|writeRpcTimeout
parameter_list|)
block|{
name|table
operator|.
name|setWriteRpcTimeout
argument_list|(
name|writeRpcTimeout
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setReadRpcTimeout
parameter_list|(
name|int
name|readRpcTimeout
parameter_list|)
block|{
name|table
operator|.
name|setReadRpcTimeout
argument_list|(
name|readRpcTimeout
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Deprecated
specifier|public
name|int
name|getRpcTimeout
parameter_list|()
block|{
return|return
name|table
operator|.
name|getRpcTimeout
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getWriteRpcTimeout
parameter_list|()
block|{
return|return
name|table
operator|.
name|getWriteRpcTimeout
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getReadRpcTimeout
parameter_list|()
block|{
return|return
name|table
operator|.
name|getReadRpcTimeout
argument_list|()
return|;
block|}
block|}
end_class

end_unit

