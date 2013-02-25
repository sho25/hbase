begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
operator|.
name|example
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
name|HashSet
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|Coprocessor
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
name|CoprocessorEnvironment
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
name|HConstants
operator|.
name|OperationStatusCode
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
name|exceptions
operator|.
name|CoprocessorException
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
name|CoprocessorService
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
name|RegionCoprocessorEnvironment
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
name|example
operator|.
name|generated
operator|.
name|BulkDeleteProtos
operator|.
name|BulkDeleteRequest
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
name|example
operator|.
name|generated
operator|.
name|BulkDeleteProtos
operator|.
name|BulkDeleteResponse
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
name|example
operator|.
name|generated
operator|.
name|BulkDeleteProtos
operator|.
name|BulkDeleteService
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
name|example
operator|.
name|generated
operator|.
name|BulkDeleteProtos
operator|.
name|BulkDeleteRequest
operator|.
name|DeleteType
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
name|example
operator|.
name|generated
operator|.
name|BulkDeleteProtos
operator|.
name|BulkDeleteResponse
operator|.
name|Builder
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
name|FirstKeyOnlyFilter
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|ResponseConverter
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
name|OperationStatus
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
name|RegionScanner
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Pair
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
name|RpcCallback
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
name|RpcController
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

begin_comment
comment|/**  * Defines a protocol to delete data in bulk based on a scan. The scan can be range scan or with  * conditions(filters) etc.This can be used to delete rows, column family(s), column qualifier(s)   * or version(s) of columns.When delete type is FAMILY or COLUMN, which all family(s) or column(s)  * getting deleted will be determined by the Scan. Scan need to select all the families/qualifiers  * which need to be deleted.When delete type is VERSION, Which column(s) and version(s) to be  * deleted will be determined by the Scan. Scan need to select all the qualifiers and its versions  * which needs to be deleted.When a timestamp is passed only one version at that timestamp will be  * deleted(even if Scan fetches many versions). When timestamp passed as null, all the versions  * which the Scan selects will get deleted.  *   *</br> Example:<code><pre>  * Scan scan = new Scan();  * // set scan properties(rowkey range, filters, timerange etc).  * HTable ht = ...;  * long noOfDeletedRows = 0L;  * Batch.Call&lt;BulkDeleteService, BulkDeleteResponse&gt; callable =   *     new Batch.Call&lt;BulkDeleteService, BulkDeleteResponse&gt;() {  *   ServerRpcController controller = new ServerRpcController();  *   BlockingRpcCallback&lt;BulkDeleteResponse&gt; rpcCallback =   *     new BlockingRpcCallback&lt;BulkDeleteResponse&gt;();  *  *   public BulkDeleteResponse call(BulkDeleteService service) throws IOException {  *     Builder builder = BulkDeleteRequest.newBuilder();  *     builder.setScan(ProtobufUtil.toScan(scan));  *     builder.setDeleteType(DeleteType.VERSION);  *     builder.setRowBatchSize(rowBatchSize);  *     // Set optional timestamp if needed  *     builder.setTimestamp(timeStamp);  *     service.delete(controller, builder.build(), rpcCallback);  *     return rpcCallback.get();  *   }  * };  * Map&lt;byte[], BulkDeleteResponse&gt; result = ht.coprocessorService(BulkDeleteService.class, scan  *     .getStartRow(), scan.getStopRow(), callable);  * for (BulkDeleteResponse response : result.values()) {  *   noOfDeletedRows += response.getRowsDeleted();  * }  *</pre></code>  */
end_comment

begin_class
specifier|public
class|class
name|BulkDeleteEndpoint
extends|extends
name|BulkDeleteService
implements|implements
name|CoprocessorService
implements|,
name|Coprocessor
block|{
specifier|private
specifier|static
specifier|final
name|String
name|NO_OF_VERSIONS_TO_DELETE
init|=
literal|"noOfVersionsToDelete"
decl_stmt|;
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
name|BulkDeleteEndpoint
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|RegionCoprocessorEnvironment
name|env
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Service
name|getService
parameter_list|()
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|delete
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|BulkDeleteRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|BulkDeleteResponse
argument_list|>
name|done
parameter_list|)
block|{
name|long
name|totalRowsDeleted
init|=
literal|0L
decl_stmt|;
name|long
name|totalVersionsDeleted
init|=
literal|0L
decl_stmt|;
name|HRegion
name|region
init|=
name|env
operator|.
name|getRegion
argument_list|()
decl_stmt|;
name|int
name|rowBatchSize
init|=
name|request
operator|.
name|getRowBatchSize
argument_list|()
decl_stmt|;
name|Long
name|timestamp
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|hasTimestamp
argument_list|()
condition|)
block|{
name|timestamp
operator|=
name|request
operator|.
name|getTimestamp
argument_list|()
expr_stmt|;
block|}
name|DeleteType
name|deleteType
init|=
name|request
operator|.
name|getDeleteType
argument_list|()
decl_stmt|;
name|boolean
name|hasMore
init|=
literal|true
decl_stmt|;
name|RegionScanner
name|scanner
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Scan
name|scan
init|=
name|ProtobufUtil
operator|.
name|toScan
argument_list|(
name|request
operator|.
name|getScan
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|scan
operator|.
name|getFilter
argument_list|()
operator|==
literal|null
operator|&&
name|deleteType
operator|==
name|DeleteType
operator|.
name|ROW
condition|)
block|{
comment|// What we need is just the rowkeys. So only 1st KV from any row is enough.
comment|// Only when it is a row delete, we can apply this filter.
comment|// In other types we rely on the scan to know which all columns to be deleted.
name|scan
operator|.
name|setFilter
argument_list|(
operator|new
name|FirstKeyOnlyFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Here by assume that the scan is perfect with the appropriate
comment|// filter and having necessary column(s).
name|scanner
operator|=
name|region
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
while|while
condition|(
name|hasMore
condition|)
block|{
name|List
argument_list|<
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|deleteRows
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
argument_list|(
name|rowBatchSize
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
name|rowBatchSize
condition|;
name|i
operator|++
control|)
block|{
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
name|hasMore
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
if|if
condition|(
name|results
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|deleteRows
operator|.
name|add
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|hasMore
condition|)
block|{
comment|// There are no more rows.
break|break;
block|}
block|}
if|if
condition|(
name|deleteRows
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Pair
argument_list|<
name|Mutation
argument_list|,
name|Integer
argument_list|>
index|[]
name|deleteWithLockArr
init|=
operator|new
name|Pair
index|[
name|deleteRows
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|deleteRow
range|:
name|deleteRows
control|)
block|{
name|Delete
name|delete
init|=
name|createDeleteMutation
argument_list|(
name|deleteRow
argument_list|,
name|deleteType
argument_list|,
name|timestamp
argument_list|)
decl_stmt|;
name|deleteWithLockArr
index|[
name|i
operator|++
index|]
operator|=
operator|new
name|Pair
argument_list|<
name|Mutation
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|delete
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|OperationStatus
index|[]
name|opStatus
init|=
name|region
operator|.
name|batchMutate
argument_list|(
name|deleteWithLockArr
argument_list|)
decl_stmt|;
for|for
control|(
name|i
operator|=
literal|0
init|;
name|i
operator|<
name|opStatus
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|opStatus
index|[
name|i
index|]
operator|.
name|getOperationStatusCode
argument_list|()
operator|!=
name|OperationStatusCode
operator|.
name|SUCCESS
condition|)
block|{
break|break;
block|}
name|totalRowsDeleted
operator|++
expr_stmt|;
if|if
condition|(
name|deleteType
operator|==
name|DeleteType
operator|.
name|VERSION
condition|)
block|{
name|byte
index|[]
name|versionsDeleted
init|=
name|deleteWithLockArr
index|[
name|i
index|]
operator|.
name|getFirst
argument_list|()
operator|.
name|getAttribute
argument_list|(
name|NO_OF_VERSIONS_TO_DELETE
argument_list|)
decl_stmt|;
if|if
condition|(
name|versionsDeleted
operator|!=
literal|null
condition|)
block|{
name|totalVersionsDeleted
operator|+=
name|Bytes
operator|.
name|toInt
argument_list|(
name|versionsDeleted
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|ioe
argument_list|)
expr_stmt|;
comment|// Call ServerRpcController#getFailedOn() to retrieve this IOException at client side.
name|ResponseConverter
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|scanner
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|Builder
name|responseBuilder
init|=
name|BulkDeleteResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|responseBuilder
operator|.
name|setRowsDeleted
argument_list|(
name|totalRowsDeleted
argument_list|)
expr_stmt|;
if|if
condition|(
name|deleteType
operator|==
name|DeleteType
operator|.
name|VERSION
condition|)
block|{
name|responseBuilder
operator|.
name|setVersionsDeleted
argument_list|(
name|totalVersionsDeleted
argument_list|)
expr_stmt|;
block|}
name|BulkDeleteResponse
name|result
init|=
name|responseBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|done
operator|.
name|run
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Delete
name|createDeleteMutation
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|deleteRow
parameter_list|,
name|DeleteType
name|deleteType
parameter_list|,
name|Long
name|timestamp
parameter_list|)
block|{
name|long
name|ts
decl_stmt|;
if|if
condition|(
name|timestamp
operator|==
literal|null
condition|)
block|{
name|ts
operator|=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
expr_stmt|;
block|}
else|else
block|{
name|ts
operator|=
name|timestamp
expr_stmt|;
block|}
comment|// We just need the rowkey. Get it from 1st KV.
name|byte
index|[]
name|row
init|=
name|deleteRow
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRow
argument_list|()
decl_stmt|;
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|row
argument_list|,
name|ts
argument_list|)
decl_stmt|;
if|if
condition|(
name|deleteType
operator|==
name|DeleteType
operator|.
name|FAMILY
condition|)
block|{
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|families
init|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|deleteRow
control|)
block|{
if|if
condition|(
name|families
operator|.
name|add
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|)
condition|)
block|{
name|delete
operator|.
name|deleteFamily
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|deleteType
operator|==
name|DeleteType
operator|.
name|COLUMN
condition|)
block|{
name|Set
argument_list|<
name|Column
argument_list|>
name|columns
init|=
operator|new
name|HashSet
argument_list|<
name|Column
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|deleteRow
control|)
block|{
name|Column
name|column
init|=
operator|new
name|Column
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|columns
operator|.
name|add
argument_list|(
name|column
argument_list|)
condition|)
block|{
comment|// Making deleteColumns() calls more than once for the same cf:qualifier is not correct
comment|// Every call to deleteColumns() will add a new KV to the familymap which will finally
comment|// get written to the memstore as part of delete().
name|delete
operator|.
name|deleteColumns
argument_list|(
name|column
operator|.
name|family
argument_list|,
name|column
operator|.
name|qualifier
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|deleteType
operator|==
name|DeleteType
operator|.
name|VERSION
condition|)
block|{
comment|// When some timestamp was passed to the delete() call only one version of the column (with
comment|// given timestamp) will be deleted. If no timestamp passed, it will delete N versions.
comment|// How many versions will get deleted depends on the Scan being passed. All the KVs that
comment|// the scan fetched will get deleted.
name|int
name|noOfVersionsToDelete
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|timestamp
operator|==
literal|null
condition|)
block|{
for|for
control|(
name|KeyValue
name|kv
range|:
name|deleteRow
control|)
block|{
name|delete
operator|.
name|deleteColumn
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|noOfVersionsToDelete
operator|++
expr_stmt|;
block|}
block|}
else|else
block|{
name|Set
argument_list|<
name|Column
argument_list|>
name|columns
init|=
operator|new
name|HashSet
argument_list|<
name|Column
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|deleteRow
control|)
block|{
name|Column
name|column
init|=
operator|new
name|Column
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|)
decl_stmt|;
comment|// Only one version of particular column getting deleted.
if|if
condition|(
name|columns
operator|.
name|add
argument_list|(
name|column
argument_list|)
condition|)
block|{
name|delete
operator|.
name|deleteColumn
argument_list|(
name|column
operator|.
name|family
argument_list|,
name|column
operator|.
name|qualifier
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|noOfVersionsToDelete
operator|++
expr_stmt|;
block|}
block|}
block|}
name|delete
operator|.
name|setAttribute
argument_list|(
name|NO_OF_VERSIONS_TO_DELETE
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|noOfVersionsToDelete
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|delete
return|;
block|}
specifier|private
specifier|static
class|class
name|Column
block|{
specifier|private
name|byte
index|[]
name|family
decl_stmt|;
specifier|private
name|byte
index|[]
name|qualifier
decl_stmt|;
specifier|public
name|Column
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
name|this
operator|.
name|family
operator|=
name|family
expr_stmt|;
name|this
operator|.
name|qualifier
operator|=
name|qualifier
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|other
operator|instanceof
name|Column
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Column
name|column
init|=
operator|(
name|Column
operator|)
name|other
decl_stmt|;
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|family
argument_list|,
name|column
operator|.
name|family
argument_list|)
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|qualifier
argument_list|,
name|column
operator|.
name|qualifier
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|h
init|=
literal|31
decl_stmt|;
name|h
operator|=
name|h
operator|+
literal|13
operator|*
name|Bytes
operator|.
name|hashCode
argument_list|(
name|this
operator|.
name|family
argument_list|)
expr_stmt|;
name|h
operator|=
name|h
operator|+
literal|13
operator|*
name|Bytes
operator|.
name|hashCode
argument_list|(
name|this
operator|.
name|qualifier
argument_list|)
expr_stmt|;
return|return
name|h
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|env
operator|instanceof
name|RegionCoprocessorEnvironment
condition|)
block|{
name|this
operator|.
name|env
operator|=
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|env
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|CoprocessorException
argument_list|(
literal|"Must be loaded on a table region!"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
comment|// nothing to do
block|}
block|}
end_class

end_unit

