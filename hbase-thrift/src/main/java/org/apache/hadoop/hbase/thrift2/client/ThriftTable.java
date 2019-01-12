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
name|thrift2
operator|.
name|client
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
name|thrift
operator|.
name|Constants
operator|.
name|HBASE_THRIFT_CLIENT_SCANNER_CACHING
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
name|thrift
operator|.
name|Constants
operator|.
name|HBASE_THRIFT_CLIENT_SCANNER_CACHING_DEFAULT
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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayDeque
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
name|concurrent
operator|.
name|TimeUnit
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
name|NotImplementedException
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
name|Row
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
name|RowMutations
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
name|client
operator|.
name|TableDescriptor
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
name|metrics
operator|.
name|ScanMetrics
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
name|TimeRange
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
name|hbase
operator|.
name|thrift2
operator|.
name|ThriftUtilities
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
name|thrift2
operator|.
name|generated
operator|.
name|TAppend
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
name|thrift2
operator|.
name|generated
operator|.
name|TDelete
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
name|thrift2
operator|.
name|generated
operator|.
name|TGet
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
name|thrift2
operator|.
name|generated
operator|.
name|THBaseService
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
name|thrift2
operator|.
name|generated
operator|.
name|TIncrement
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
name|thrift2
operator|.
name|generated
operator|.
name|TPut
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
name|thrift2
operator|.
name|generated
operator|.
name|TResult
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
name|thrift2
operator|.
name|generated
operator|.
name|TRowMutations
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
name|thrift2
operator|.
name|generated
operator|.
name|TScan
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
name|thrift2
operator|.
name|generated
operator|.
name|TTableDescriptor
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
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransport
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
name|hbase
operator|.
name|thirdparty
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|primitives
operator|.
name|Booleans
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ThriftTable
implements|implements
name|Table
block|{
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|TTransport
name|tTransport
decl_stmt|;
specifier|private
name|THBaseService
operator|.
name|Client
name|client
decl_stmt|;
specifier|private
name|ByteBuffer
name|tableNameInBytes
decl_stmt|;
specifier|private
name|int
name|operationTimeout
decl_stmt|;
specifier|private
specifier|final
name|int
name|scannerCaching
decl_stmt|;
specifier|public
name|ThriftTable
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|THBaseService
operator|.
name|Client
name|client
parameter_list|,
name|TTransport
name|tTransport
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|tableNameInBytes
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|tableName
operator|.
name|toBytes
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|tTransport
operator|=
name|tTransport
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|scannerCaching
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HBASE_THRIFT_CLIENT_SCANNER_CACHING
argument_list|,
name|HBASE_THRIFT_CLIENT_SCANNER_CACHING_DEFAULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|operationTimeout
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TableName
name|getName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
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
try|try
block|{
name|TTableDescriptor
name|tableDescriptor
init|=
name|client
operator|.
name|getTableDescriptor
argument_list|(
name|ThriftUtilities
operator|.
name|tableNameFromHBase
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|ThriftUtilities
operator|.
name|tableDescriptorFromThrift
argument_list|(
name|tableDescriptor
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
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
name|TGet
name|tGet
init|=
name|ThriftUtilities
operator|.
name|getFromHBase
argument_list|(
name|get
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|client
operator|.
name|exists
argument_list|(
name|tableNameInBytes
argument_list|,
name|tGet
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
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
name|List
argument_list|<
name|TGet
argument_list|>
name|tGets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Get
name|get
range|:
name|gets
control|)
block|{
name|tGets
operator|.
name|add
argument_list|(
name|ThriftUtilities
operator|.
name|getFromHBase
argument_list|(
name|get
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|List
argument_list|<
name|Boolean
argument_list|>
name|results
init|=
name|client
operator|.
name|existsAll
argument_list|(
name|tableNameInBytes
argument_list|,
name|tGets
argument_list|)
decl_stmt|;
return|return
name|Booleans
operator|.
name|toArray
argument_list|(
name|results
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
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
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Batch not supported in ThriftTable, use put(List<Put> puts), "
operator|+
literal|"get(List<Get> gets) or delete(List<Delete> deletes) respectively"
argument_list|)
throw|;
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
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"BatchCallback not supported in ThriftTable, use put(List<Put> puts), "
operator|+
literal|"get(List<Get> gets) or delete(List<Delete> deletes) respectively"
argument_list|)
throw|;
block|}
annotation|@
name|Override
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
name|TGet
name|tGet
init|=
name|ThriftUtilities
operator|.
name|getFromHBase
argument_list|(
name|get
argument_list|)
decl_stmt|;
try|try
block|{
name|TResult
name|tResult
init|=
name|client
operator|.
name|get
argument_list|(
name|tableNameInBytes
argument_list|,
name|tGet
argument_list|)
decl_stmt|;
return|return
name|ThriftUtilities
operator|.
name|resultFromThrift
argument_list|(
name|tResult
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
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
name|List
argument_list|<
name|TGet
argument_list|>
name|tGets
init|=
name|ThriftUtilities
operator|.
name|getsFromHBase
argument_list|(
name|gets
argument_list|)
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|TResult
argument_list|>
name|results
init|=
name|client
operator|.
name|getMultiple
argument_list|(
name|tableNameInBytes
argument_list|,
name|tGets
argument_list|)
decl_stmt|;
return|return
name|ThriftUtilities
operator|.
name|resultsFromThrift
argument_list|(
name|results
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * A scanner to perform scan from thrift server    * getScannerResults is used in this scanner    */
specifier|private
class|class
name|Scanner
implements|implements
name|ResultScanner
block|{
specifier|protected
name|TScan
name|scan
decl_stmt|;
specifier|protected
name|Result
name|lastResult
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|final
name|Queue
argument_list|<
name|Result
argument_list|>
name|cache
init|=
operator|new
name|ArrayDeque
argument_list|<>
argument_list|()
decl_stmt|;
empty_stmt|;
specifier|public
name|Scanner
parameter_list|(
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|scan
operator|.
name|getBatch
argument_list|()
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Batch is not supported in Scanner"
argument_list|)
throw|;
block|}
if|if
condition|(
name|scan
operator|.
name|getCaching
argument_list|()
operator|<=
literal|0
condition|)
block|{
name|scan
operator|.
name|setCaching
argument_list|(
name|scannerCaching
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|scan
operator|.
name|getCaching
argument_list|()
operator|==
literal|1
operator|&&
name|scan
operator|.
name|isReversed
argument_list|()
condition|)
block|{
comment|// for reverse scan, we need to pass the last row to the next scanner
comment|// we need caching number bigger than 1
name|scan
operator|.
name|setCaching
argument_list|(
name|scan
operator|.
name|getCaching
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|scan
operator|=
name|ThriftUtilities
operator|.
name|scanFromHBase
argument_list|(
name|scan
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|next
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|cache
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|setupNextScanner
argument_list|()
expr_stmt|;
try|try
block|{
name|List
argument_list|<
name|TResult
argument_list|>
name|tResults
init|=
name|client
operator|.
name|getScannerResults
argument_list|(
name|tableNameInBytes
argument_list|,
name|scan
argument_list|,
name|scan
operator|.
name|getCaching
argument_list|()
argument_list|)
decl_stmt|;
name|Result
index|[]
name|results
init|=
name|ThriftUtilities
operator|.
name|resultsFromThrift
argument_list|(
name|tResults
argument_list|)
decl_stmt|;
name|boolean
name|firstKey
init|=
literal|true
decl_stmt|;
for|for
control|(
name|Result
name|result
range|:
name|results
control|)
block|{
comment|// If it is a reverse scan, we use the last result's key as the startkey, since there is
comment|// no way to construct a closet rowkey smaller than the last result
comment|// So when the results return, we must rule out the first result, since it has already
comment|// returned to user.
if|if
condition|(
name|firstKey
condition|)
block|{
name|firstKey
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|scan
operator|.
name|isReversed
argument_list|()
operator|&&
name|lastResult
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|lastResult
operator|.
name|getRow
argument_list|()
argument_list|,
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
block|}
block|}
name|cache
operator|.
name|add
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|lastResult
operator|=
name|result
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|cache
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
return|return
name|cache
operator|.
name|poll
argument_list|()
return|;
block|}
else|else
block|{
comment|//scan finished
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{     }
annotation|@
name|Override
specifier|public
name|boolean
name|renewLease
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"renewLease() not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|ScanMetrics
name|getScanMetrics
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"getScanMetrics() not supported"
argument_list|)
throw|;
block|}
specifier|private
name|void
name|setupNextScanner
parameter_list|()
block|{
comment|//if lastResult is null null, it means it is not the fist scan
if|if
condition|(
name|lastResult
operator|!=
literal|null
condition|)
block|{
name|byte
index|[]
name|lastRow
init|=
name|lastResult
operator|.
name|getRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|scan
operator|.
name|isReversed
argument_list|()
condition|)
block|{
comment|//for reverse scan, we can't find the closet row before this row
name|scan
operator|.
name|setStartRow
argument_list|(
name|lastRow
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|scan
operator|.
name|setStartRow
argument_list|(
name|createClosestRowAfter
argument_list|(
name|lastRow
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Create the closest row after the specified row      */
specifier|protected
name|byte
index|[]
name|createClosestRowAfter
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
block|{
if|if
condition|(
name|row
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"The passed row is null"
argument_list|)
throw|;
block|}
return|return
name|Arrays
operator|.
name|copyOf
argument_list|(
name|row
argument_list|,
name|row
operator|.
name|length
operator|+
literal|1
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
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
operator|new
name|Scanner
argument_list|(
name|scan
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|family
argument_list|)
expr_stmt|;
return|return
name|getScanner
argument_list|(
name|scan
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
return|return
name|getScanner
argument_list|(
name|scan
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TPut
name|tPut
init|=
name|ThriftUtilities
operator|.
name|putFromHBase
argument_list|(
name|put
argument_list|)
decl_stmt|;
try|try
block|{
name|client
operator|.
name|put
argument_list|(
name|tableNameInBytes
argument_list|,
name|tPut
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
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
name|List
argument_list|<
name|TPut
argument_list|>
name|tPuts
init|=
name|ThriftUtilities
operator|.
name|putsFromHBase
argument_list|(
name|puts
argument_list|)
decl_stmt|;
try|try
block|{
name|client
operator|.
name|putMultiple
argument_list|(
name|tableNameInBytes
argument_list|,
name|tPuts
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
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
name|TDelete
name|tDelete
init|=
name|ThriftUtilities
operator|.
name|deleteFromHBase
argument_list|(
name|delete
argument_list|)
decl_stmt|;
try|try
block|{
name|client
operator|.
name|deleteSingle
argument_list|(
name|tableNameInBytes
argument_list|,
name|tDelete
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
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
name|List
argument_list|<
name|TDelete
argument_list|>
name|tDeletes
init|=
name|ThriftUtilities
operator|.
name|deletesFromHBase
argument_list|(
name|deletes
argument_list|)
decl_stmt|;
try|try
block|{
name|client
operator|.
name|deleteMultiple
argument_list|(
name|tableNameInBytes
argument_list|,
name|tDeletes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
class|class
name|CheckAndMutateBuilderImpl
implements|implements
name|CheckAndMutateBuilder
block|{
specifier|private
specifier|final
name|byte
index|[]
name|row
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|family
decl_stmt|;
specifier|private
name|byte
index|[]
name|qualifier
decl_stmt|;
specifier|private
name|CompareOperator
name|op
decl_stmt|;
specifier|private
name|byte
index|[]
name|value
decl_stmt|;
name|CheckAndMutateBuilderImpl
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|row
argument_list|,
literal|"row is null"
argument_list|)
expr_stmt|;
name|this
operator|.
name|family
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|family
argument_list|,
literal|"family is null"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|CheckAndMutateBuilder
name|qualifier
parameter_list|(
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
name|this
operator|.
name|qualifier
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|qualifier
argument_list|,
literal|"qualifier is null. Consider using"
operator|+
literal|" an empty byte array, or just do not call this method if you want a null qualifier"
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|CheckAndMutateBuilder
name|timeRange
parameter_list|(
name|TimeRange
name|timeRange
parameter_list|)
block|{
throw|throw
operator|new
name|NotImplementedException
argument_list|(
literal|"timeRange not supported in ThriftTable"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|CheckAndMutateBuilder
name|ifNotExists
parameter_list|()
block|{
name|this
operator|.
name|op
operator|=
name|CompareOperator
operator|.
name|EQUAL
expr_stmt|;
name|this
operator|.
name|value
operator|=
literal|null
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|CheckAndMutateBuilder
name|ifMatches
parameter_list|(
name|CompareOperator
name|compareOp
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
operator|.
name|op
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|compareOp
argument_list|,
literal|"compareOp is null"
argument_list|)
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|value
argument_list|,
literal|"value is null"
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|private
name|void
name|preCheck
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|op
argument_list|,
literal|"condition is null. You need to specify the condition by"
operator|+
literal|" calling ifNotExists/ifEquals/ifMatches before executing the request"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|thenPut
parameter_list|(
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{
name|preCheck
argument_list|()
expr_stmt|;
name|RowMutations
name|rowMutations
init|=
operator|new
name|RowMutations
argument_list|(
name|put
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|rowMutations
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
return|return
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
name|rowMutations
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|thenDelete
parameter_list|(
name|Delete
name|delete
parameter_list|)
throws|throws
name|IOException
block|{
name|preCheck
argument_list|()
expr_stmt|;
name|RowMutations
name|rowMutations
init|=
operator|new
name|RowMutations
argument_list|(
name|delete
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|rowMutations
operator|.
name|add
argument_list|(
name|delete
argument_list|)
expr_stmt|;
return|return
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
name|rowMutations
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|thenMutate
parameter_list|(
name|RowMutations
name|mutation
parameter_list|)
throws|throws
name|IOException
block|{
name|preCheck
argument_list|()
expr_stmt|;
return|return
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
name|mutation
argument_list|)
return|;
block|}
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
name|mutation
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|ByteBuffer
name|valueBuffer
init|=
name|value
operator|==
literal|null
condition|?
literal|null
else|:
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|value
argument_list|)
decl_stmt|;
return|return
name|client
operator|.
name|checkAndMutate
argument_list|(
name|tableNameInBytes
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|family
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|qualifier
argument_list|)
argument_list|,
name|ThriftUtilities
operator|.
name|compareOpFromHBase
argument_list|(
name|op
argument_list|)
argument_list|,
name|valueBuffer
argument_list|,
name|ThriftUtilities
operator|.
name|rowMutationsFromHBase
argument_list|(
name|mutation
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|CheckAndMutateBuilder
name|checkAndMutate
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|)
block|{
return|return
operator|new
name|CheckAndMutateBuilderImpl
argument_list|(
name|row
argument_list|,
name|family
argument_list|)
return|;
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
name|TRowMutations
name|tRowMutations
init|=
name|ThriftUtilities
operator|.
name|rowMutationsFromHBase
argument_list|(
name|rm
argument_list|)
decl_stmt|;
try|try
block|{
name|client
operator|.
name|mutateRow
argument_list|(
name|tableNameInBytes
argument_list|,
name|tRowMutations
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
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
name|TAppend
name|tAppend
init|=
name|ThriftUtilities
operator|.
name|appendFromHBase
argument_list|(
name|append
argument_list|)
decl_stmt|;
try|try
block|{
name|TResult
name|tResult
init|=
name|client
operator|.
name|append
argument_list|(
name|tableNameInBytes
argument_list|,
name|tAppend
argument_list|)
decl_stmt|;
return|return
name|ThriftUtilities
operator|.
name|resultFromThrift
argument_list|(
name|tResult
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
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
name|TIncrement
name|tIncrement
init|=
name|ThriftUtilities
operator|.
name|incrementFromHBase
argument_list|(
name|increment
argument_list|)
decl_stmt|;
try|try
block|{
name|TResult
name|tResult
init|=
name|client
operator|.
name|increment
argument_list|(
name|tableNameInBytes
argument_list|,
name|tIncrement
argument_list|)
decl_stmt|;
return|return
name|ThriftUtilities
operator|.
name|resultFromThrift
argument_list|(
name|tResult
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|tTransport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getRpcTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|unit
operator|.
name|convert
argument_list|(
name|operationTimeout
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getReadRpcTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|unit
operator|.
name|convert
argument_list|(
name|operationTimeout
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getWriteRpcTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|unit
operator|.
name|convert
argument_list|(
name|operationTimeout
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getOperationTimeout
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|unit
operator|.
name|convert
argument_list|(
name|operationTimeout
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
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
throw|throw
operator|new
name|NotImplementedException
argument_list|(
literal|"coprocessorService not supported in ThriftTable"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit
