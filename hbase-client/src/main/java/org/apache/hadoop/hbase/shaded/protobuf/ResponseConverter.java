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
name|shaded
operator|.
name|protobuf
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
name|HashMap
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
name|CellScanner
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
name|DoNotRetryIOException
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
name|HRegionInfo
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
name|ServerName
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
name|classification
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
name|SingleResponse
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
name|ServerRpcController
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
name|AdminProtos
operator|.
name|CloseRegionResponse
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
name|AdminProtos
operator|.
name|GetOnlineRegionResponse
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
name|AdminProtos
operator|.
name|GetServerInfoResponse
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
name|AdminProtos
operator|.
name|OpenRegionResponse
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
name|AdminProtos
operator|.
name|ServerInfo
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
name|MultiRequest
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
name|MultiResponse
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
name|RegionAction
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
name|RegionActionResult
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
name|ResultOrException
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
name|ScanResponse
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
name|ClusterStatusProtos
operator|.
name|RegionStoreSequenceIds
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
name|HBaseProtos
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
name|HBaseProtos
operator|.
name|NameBytesPair
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
name|HBaseProtos
operator|.
name|NameInt64Pair
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
name|MapReduceProtos
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|MasterProtos
operator|.
name|EnableCatalogJanitorResponse
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
name|MasterProtos
operator|.
name|RunCatalogScanResponse
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
name|MasterProtos
operator|.
name|RunCleanerChoreResponse
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
name|RegionServerStatusProtos
operator|.
name|GetLastFlushedSequenceIdResponse
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
name|RegionOpeningState
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
name|protobuf
operator|.
name|ByteString
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
name|protobuf
operator|.
name|RpcController
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
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|Nullable
import|;
end_import

begin_comment
comment|/**  * Helper utility to build protocol buffer responses,  * or retrieve data from protocol buffer responses.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ResponseConverter
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
name|ResponseConverter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ResponseConverter
parameter_list|()
block|{   }
comment|// Start utilities for Client
specifier|public
specifier|static
name|SingleResponse
name|getResult
parameter_list|(
specifier|final
name|ClientProtos
operator|.
name|MutateRequest
name|request
parameter_list|,
specifier|final
name|ClientProtos
operator|.
name|MutateResponse
name|response
parameter_list|,
specifier|final
name|CellScanner
name|cells
parameter_list|)
throws|throws
name|IOException
block|{
name|SingleResponse
name|singleResponse
init|=
operator|new
name|SingleResponse
argument_list|()
decl_stmt|;
name|SingleResponse
operator|.
name|Entry
name|entry
init|=
operator|new
name|SingleResponse
operator|.
name|Entry
argument_list|()
decl_stmt|;
name|entry
operator|.
name|setResult
argument_list|(
name|ProtobufUtil
operator|.
name|toResult
argument_list|(
name|response
operator|.
name|getResult
argument_list|()
argument_list|,
name|cells
argument_list|)
argument_list|)
expr_stmt|;
name|entry
operator|.
name|setProcessed
argument_list|(
name|response
operator|.
name|getProcessed
argument_list|()
argument_list|)
expr_stmt|;
name|singleResponse
operator|.
name|setEntry
argument_list|(
name|entry
argument_list|)
expr_stmt|;
return|return
name|singleResponse
return|;
block|}
comment|/**    * Get the results from a protocol buffer MultiResponse    *    * @param request the original protocol buffer MultiRequest    * @param response the protocol buffer MultiResponse to convert    * @param cells Cells to go with the passed in<code>proto</code>.  Can be null.    * @return the results that were in the MultiResponse (a Result or an Exception).    * @throws IOException    */
specifier|public
specifier|static
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
name|MultiResponse
name|getResults
parameter_list|(
specifier|final
name|MultiRequest
name|request
parameter_list|,
specifier|final
name|MultiResponse
name|response
parameter_list|,
specifier|final
name|CellScanner
name|cells
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getResults
argument_list|(
name|request
argument_list|,
literal|null
argument_list|,
name|response
argument_list|,
name|cells
argument_list|)
return|;
block|}
comment|/**    * Get the results from a protocol buffer MultiResponse    *    * @param request the original protocol buffer MultiRequest    * @param rowMutationsIndexMap Used to support RowMutations in batch    * @param response the protocol buffer MultiResponse to convert    * @param cells Cells to go with the passed in<code>proto</code>.  Can be null.    * @return the results that were in the MultiResponse (a Result or an Exception).    * @throws IOException    */
specifier|public
specifier|static
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
name|MultiResponse
name|getResults
parameter_list|(
specifier|final
name|MultiRequest
name|request
parameter_list|,
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|rowMutationsIndexMap
parameter_list|,
specifier|final
name|MultiResponse
name|response
parameter_list|,
specifier|final
name|CellScanner
name|cells
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|requestRegionActionCount
init|=
name|request
operator|.
name|getRegionActionCount
argument_list|()
decl_stmt|;
name|int
name|responseRegionActionResultCount
init|=
name|response
operator|.
name|getRegionActionResultCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|requestRegionActionCount
operator|!=
name|responseRegionActionResultCount
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Request mutation count="
operator|+
name|requestRegionActionCount
operator|+
literal|" does not match response mutation result count="
operator|+
name|responseRegionActionResultCount
argument_list|)
throw|;
block|}
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
name|MultiResponse
name|results
init|=
operator|new
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
name|MultiResponse
argument_list|()
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
name|responseRegionActionResultCount
condition|;
name|i
operator|++
control|)
block|{
name|RegionAction
name|actions
init|=
name|request
operator|.
name|getRegionAction
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|RegionActionResult
name|actionResult
init|=
name|response
operator|.
name|getRegionActionResult
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|HBaseProtos
operator|.
name|RegionSpecifier
name|rs
init|=
name|actions
operator|.
name|getRegion
argument_list|()
decl_stmt|;
if|if
condition|(
name|rs
operator|.
name|hasType
argument_list|()
operator|&&
operator|(
name|rs
operator|.
name|getType
argument_list|()
operator|!=
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|RegionSpecifierType
operator|.
name|REGION_NAME
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"We support only encoded types for protobuf multi response."
argument_list|)
throw|;
block|}
name|byte
index|[]
name|regionName
init|=
name|rs
operator|.
name|getValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
if|if
condition|(
name|actionResult
operator|.
name|hasException
argument_list|()
condition|)
block|{
name|Throwable
name|regionException
init|=
name|ProtobufUtil
operator|.
name|toException
argument_list|(
name|actionResult
operator|.
name|getException
argument_list|()
argument_list|)
decl_stmt|;
name|results
operator|.
name|addException
argument_list|(
name|regionName
argument_list|,
name|regionException
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|actions
operator|.
name|getActionCount
argument_list|()
operator|!=
name|actionResult
operator|.
name|getResultOrExceptionCount
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"actions.getActionCount="
operator|+
name|actions
operator|.
name|getActionCount
argument_list|()
operator|+
literal|", actionResult.getResultOrExceptionCount="
operator|+
name|actionResult
operator|.
name|getResultOrExceptionCount
argument_list|()
operator|+
literal|" for region "
operator|+
name|actions
operator|.
name|getRegion
argument_list|()
argument_list|)
throw|;
block|}
name|Object
name|responseValue
decl_stmt|;
comment|// For RowMutations action, if there is an exception, the exception is set
comment|// at the RegionActionResult level and the ResultOrException is null at the original index
name|Integer
name|rowMutationsIndex
init|=
operator|(
name|rowMutationsIndexMap
operator|==
literal|null
condition|?
literal|null
else|:
name|rowMutationsIndexMap
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|)
decl_stmt|;
if|if
condition|(
name|rowMutationsIndex
operator|!=
literal|null
condition|)
block|{
comment|// This RegionAction is from a RowMutations in a batch.
comment|// If there is an exception from the server, the exception is set at
comment|// the RegionActionResult level, which has been handled above.
name|responseValue
operator|=
name|response
operator|.
name|getProcessed
argument_list|()
condition|?
name|ProtobufUtil
operator|.
name|EMPTY_RESULT_EXISTS_TRUE
else|:
name|ProtobufUtil
operator|.
name|EMPTY_RESULT_EXISTS_FALSE
expr_stmt|;
name|results
operator|.
name|add
argument_list|(
name|regionName
argument_list|,
name|rowMutationsIndex
argument_list|,
name|responseValue
argument_list|)
expr_stmt|;
continue|continue;
block|}
for|for
control|(
name|ResultOrException
name|roe
range|:
name|actionResult
operator|.
name|getResultOrExceptionList
argument_list|()
control|)
block|{
if|if
condition|(
name|roe
operator|.
name|hasException
argument_list|()
condition|)
block|{
name|responseValue
operator|=
name|ProtobufUtil
operator|.
name|toException
argument_list|(
name|roe
operator|.
name|getException
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|roe
operator|.
name|hasResult
argument_list|()
condition|)
block|{
name|responseValue
operator|=
name|ProtobufUtil
operator|.
name|toResult
argument_list|(
name|roe
operator|.
name|getResult
argument_list|()
argument_list|,
name|cells
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|roe
operator|.
name|hasServiceResult
argument_list|()
condition|)
block|{
name|responseValue
operator|=
name|roe
operator|.
name|getServiceResult
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// Sometimes, the response is just "it was processed". Generally, this occurs for things
comment|// like mutateRows where either we get back 'processed' (or not) and optionally some
comment|// statistics about the regions we touched.
name|responseValue
operator|=
name|response
operator|.
name|getProcessed
argument_list|()
condition|?
name|ProtobufUtil
operator|.
name|EMPTY_RESULT_EXISTS_TRUE
else|:
name|ProtobufUtil
operator|.
name|EMPTY_RESULT_EXISTS_FALSE
expr_stmt|;
block|}
name|results
operator|.
name|add
argument_list|(
name|regionName
argument_list|,
name|roe
operator|.
name|getIndex
argument_list|()
argument_list|,
name|responseValue
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|response
operator|.
name|hasRegionStatistics
argument_list|()
condition|)
block|{
name|ClientProtos
operator|.
name|MultiRegionLoadStats
name|stats
init|=
name|response
operator|.
name|getRegionStatistics
argument_list|()
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
name|stats
operator|.
name|getRegionCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|results
operator|.
name|addStatistic
argument_list|(
name|stats
operator|.
name|getRegion
argument_list|(
name|i
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|stats
operator|.
name|getStat
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|results
return|;
block|}
comment|/**    * Wrap a throwable to an action result.    *    * @param t    * @return an action result builder    */
specifier|public
specifier|static
name|ResultOrException
operator|.
name|Builder
name|buildActionResult
parameter_list|(
specifier|final
name|Throwable
name|t
parameter_list|)
block|{
name|ResultOrException
operator|.
name|Builder
name|builder
init|=
name|ResultOrException
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|t
operator|!=
literal|null
condition|)
name|builder
operator|.
name|setException
argument_list|(
name|buildException
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
comment|/**    * Wrap a throwable to an action result.    *    * @param r    * @return an action result builder    */
specifier|public
specifier|static
name|ResultOrException
operator|.
name|Builder
name|buildActionResult
parameter_list|(
specifier|final
name|ClientProtos
operator|.
name|Result
name|r
parameter_list|)
block|{
name|ResultOrException
operator|.
name|Builder
name|builder
init|=
name|ResultOrException
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|r
operator|!=
literal|null
condition|)
name|builder
operator|.
name|setResult
argument_list|(
name|r
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
comment|/**    * @param t    * @return NameValuePair of the exception name to stringified version os exception.    */
specifier|public
specifier|static
name|NameBytesPair
name|buildException
parameter_list|(
specifier|final
name|Throwable
name|t
parameter_list|)
block|{
name|NameBytesPair
operator|.
name|Builder
name|parameterBuilder
init|=
name|NameBytesPair
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|parameterBuilder
operator|.
name|setName
argument_list|(
name|t
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|parameterBuilder
operator|.
name|setValue
argument_list|(
name|ByteString
operator|.
name|copyFromUtf8
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|t
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|parameterBuilder
operator|.
name|build
argument_list|()
return|;
block|}
comment|// End utilities for Client
comment|// Start utilities for Admin
comment|/**    * Get the list of region info from a GetOnlineRegionResponse    *    * @param proto the GetOnlineRegionResponse    * @return the list of region info    */
specifier|public
specifier|static
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|getRegionInfos
parameter_list|(
specifier|final
name|GetOnlineRegionResponse
name|proto
parameter_list|)
block|{
if|if
condition|(
name|proto
operator|==
literal|null
operator|||
name|proto
operator|.
name|getRegionInfoCount
argument_list|()
operator|==
literal|0
condition|)
return|return
literal|null
return|;
return|return
name|ProtobufUtil
operator|.
name|getRegionInfos
argument_list|(
name|proto
argument_list|)
return|;
block|}
comment|/**    * Get the region opening state from a OpenRegionResponse    *    * @param proto the OpenRegionResponse    * @return the region opening state    */
specifier|public
specifier|static
name|RegionOpeningState
name|getRegionOpeningState
parameter_list|(
specifier|final
name|OpenRegionResponse
name|proto
parameter_list|)
block|{
if|if
condition|(
name|proto
operator|==
literal|null
operator|||
name|proto
operator|.
name|getOpeningStateCount
argument_list|()
operator|!=
literal|1
condition|)
return|return
literal|null
return|;
return|return
name|RegionOpeningState
operator|.
name|valueOf
argument_list|(
name|proto
operator|.
name|getOpeningState
argument_list|(
literal|0
argument_list|)
operator|.
name|name
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Get a list of region opening state from a OpenRegionResponse    *    * @param proto the OpenRegionResponse    * @return the list of region opening state    */
specifier|public
specifier|static
name|List
argument_list|<
name|RegionOpeningState
argument_list|>
name|getRegionOpeningStateList
parameter_list|(
specifier|final
name|OpenRegionResponse
name|proto
parameter_list|)
block|{
if|if
condition|(
name|proto
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|List
argument_list|<
name|RegionOpeningState
argument_list|>
name|regionOpeningStates
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|proto
operator|.
name|getOpeningStateCount
argument_list|()
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
name|proto
operator|.
name|getOpeningStateCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|regionOpeningStates
operator|.
name|add
argument_list|(
name|RegionOpeningState
operator|.
name|valueOf
argument_list|(
name|proto
operator|.
name|getOpeningState
argument_list|(
name|i
argument_list|)
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|regionOpeningStates
return|;
block|}
comment|/**    * Check if the region is closed from a CloseRegionResponse    *    * @param proto the CloseRegionResponse    * @return the region close state    */
specifier|public
specifier|static
name|boolean
name|isClosed
parameter_list|(
specifier|final
name|CloseRegionResponse
name|proto
parameter_list|)
block|{
if|if
condition|(
name|proto
operator|==
literal|null
operator|||
operator|!
name|proto
operator|.
name|hasClosed
argument_list|()
condition|)
return|return
literal|false
return|;
return|return
name|proto
operator|.
name|getClosed
argument_list|()
return|;
block|}
comment|/**    * A utility to build a GetServerInfoResponse.    *    * @param serverName    * @param webuiPort    * @return the response    */
specifier|public
specifier|static
name|GetServerInfoResponse
name|buildGetServerInfoResponse
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|int
name|webuiPort
parameter_list|)
block|{
name|GetServerInfoResponse
operator|.
name|Builder
name|builder
init|=
name|GetServerInfoResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|ServerInfo
operator|.
name|Builder
name|serverInfoBuilder
init|=
name|ServerInfo
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|serverInfoBuilder
operator|.
name|setServerName
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|serverName
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|webuiPort
operator|>=
literal|0
condition|)
block|{
name|serverInfoBuilder
operator|.
name|setWebuiPort
argument_list|(
name|webuiPort
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|setServerInfo
argument_list|(
name|serverInfoBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * A utility to build a GetOnlineRegionResponse.    *    * @param regions    * @return the response    */
specifier|public
specifier|static
name|GetOnlineRegionResponse
name|buildGetOnlineRegionResponse
parameter_list|(
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
block|{
name|GetOnlineRegionResponse
operator|.
name|Builder
name|builder
init|=
name|GetOnlineRegionResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|region
range|:
name|regions
control|)
block|{
name|builder
operator|.
name|addRegionInfo
argument_list|(
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|region
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Creates a response for the catalog scan request    * @return A RunCatalogScanResponse    */
specifier|public
specifier|static
name|RunCatalogScanResponse
name|buildRunCatalogScanResponse
parameter_list|(
name|int
name|numCleaned
parameter_list|)
block|{
return|return
name|RunCatalogScanResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setScanResult
argument_list|(
name|numCleaned
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Creates a response for the catalog scan request    * @return A EnableCatalogJanitorResponse    */
specifier|public
specifier|static
name|EnableCatalogJanitorResponse
name|buildEnableCatalogJanitorResponse
parameter_list|(
name|boolean
name|prevValue
parameter_list|)
block|{
return|return
name|EnableCatalogJanitorResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setPrevValue
argument_list|(
name|prevValue
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Creates a response for the cleaner chore request    * @return A RunCleanerChoreResponse    */
specifier|public
specifier|static
name|RunCleanerChoreResponse
name|buildRunCleanerChoreResponse
parameter_list|(
name|boolean
name|ran
parameter_list|)
block|{
return|return
name|RunCleanerChoreResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setCleanerChoreRan
argument_list|(
name|ran
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|// End utilities for Admin
comment|/**    * Creates a response for the last flushed sequence Id request    * @return A GetLastFlushedSequenceIdResponse    */
specifier|public
specifier|static
name|GetLastFlushedSequenceIdResponse
name|buildGetLastFlushedSequenceIdResponse
parameter_list|(
name|RegionStoreSequenceIds
name|ids
parameter_list|)
block|{
return|return
name|GetLastFlushedSequenceIdResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setLastFlushedSequenceId
argument_list|(
name|ids
operator|.
name|getLastFlushedSequenceId
argument_list|()
argument_list|)
operator|.
name|addAllStoreLastFlushedSequenceId
argument_list|(
name|ids
operator|.
name|getStoreSequenceIdList
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Stores an exception encountered during RPC invocation so it can be passed back    * through to the client.    * @param controller the controller instance provided by the client when calling the service    * @param ioe the exception encountered    */
specifier|public
specifier|static
name|void
name|setControllerException
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcController
name|controller
parameter_list|,
name|IOException
name|ioe
parameter_list|)
block|{
if|if
condition|(
name|controller
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|controller
operator|instanceof
name|ServerRpcController
condition|)
block|{
operator|(
operator|(
name|ServerRpcController
operator|)
name|controller
operator|)
operator|.
name|setFailedOn
argument_list|(
name|ioe
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|controller
operator|.
name|setFailed
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|ioe
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Retreivies exception stored during RPC invocation.    * @param controller the controller instance provided by the client when calling the service    * @return exception if any, or null; Will return DoNotRetryIOException for string represented    * failure causes in controller.    */
annotation|@
name|Nullable
specifier|public
specifier|static
name|IOException
name|getControllerException
parameter_list|(
name|RpcController
name|controller
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|controller
operator|!=
literal|null
operator|&&
name|controller
operator|.
name|failed
argument_list|()
condition|)
block|{
if|if
condition|(
name|controller
operator|instanceof
name|ServerRpcController
condition|)
block|{
return|return
operator|(
operator|(
name|ServerRpcController
operator|)
name|controller
operator|)
operator|.
name|getFailedOn
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|new
name|DoNotRetryIOException
argument_list|(
name|controller
operator|.
name|errorText
argument_list|()
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Create Results from the cells using the cells meta data.    * @param cellScanner    * @param response    * @return results    */
specifier|public
specifier|static
name|Result
index|[]
name|getResults
parameter_list|(
name|CellScanner
name|cellScanner
parameter_list|,
name|ScanResponse
name|response
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|response
operator|==
literal|null
condition|)
return|return
literal|null
return|;
comment|// If cellscanner, then the number of Results to return is the count of elements in the
comment|// cellsPerResult list.  Otherwise, it is how many results are embedded inside the response.
name|int
name|noOfResults
init|=
name|cellScanner
operator|!=
literal|null
condition|?
name|response
operator|.
name|getCellsPerResultCount
argument_list|()
else|:
name|response
operator|.
name|getResultsCount
argument_list|()
decl_stmt|;
name|Result
index|[]
name|results
init|=
operator|new
name|Result
index|[
name|noOfResults
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
name|noOfResults
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|cellScanner
operator|!=
literal|null
condition|)
block|{
comment|// Cells are out in cellblocks.  Group them up again as Results.  How many to read at a
comment|// time will be found in getCellsLength -- length here is how many Cells in the i'th Result
name|int
name|noOfCells
init|=
name|response
operator|.
name|getCellsPerResult
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|boolean
name|isPartial
init|=
name|response
operator|.
name|getPartialFlagPerResultCount
argument_list|()
operator|>
name|i
condition|?
name|response
operator|.
name|getPartialFlagPerResult
argument_list|(
name|i
argument_list|)
else|:
literal|false
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|noOfCells
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|noOfCells
condition|;
name|j
operator|++
control|)
block|{
try|try
block|{
if|if
condition|(
name|cellScanner
operator|.
name|advance
argument_list|()
operator|==
literal|false
condition|)
block|{
comment|// We are not able to retrieve the exact number of cells which ResultCellMeta says us.
comment|// We have to scan for the same results again. Throwing DNRIOE as a client retry on the
comment|// same scanner will result in OutOfOrderScannerNextException
name|String
name|msg
init|=
literal|"Results sent from server="
operator|+
name|noOfResults
operator|+
literal|". But only got "
operator|+
name|i
operator|+
literal|" results completely at client. Resetting the scanner to scan again."
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|// We are getting IOE while retrieving the cells for Results.
comment|// We have to scan for the same results again. Throwing DNRIOE as a client retry on the
comment|// same scanner will result in OutOfOrderScannerNextException
name|LOG
operator|.
name|error
argument_list|(
literal|"Exception while reading cells from result."
operator|+
literal|"Resetting the scanner to scan again."
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Resetting the scanner."
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
name|cells
operator|.
name|add
argument_list|(
name|cellScanner
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|results
index|[
name|i
index|]
operator|=
name|Result
operator|.
name|create
argument_list|(
name|cells
argument_list|,
literal|null
argument_list|,
name|response
operator|.
name|getStale
argument_list|()
argument_list|,
name|isPartial
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Result is pure pb.
name|results
index|[
name|i
index|]
operator|=
name|ProtobufUtil
operator|.
name|toResult
argument_list|(
name|response
operator|.
name|getResults
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|results
return|;
block|}
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getScanMetrics
parameter_list|(
name|ScanResponse
name|response
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|metricMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|response
operator|==
literal|null
operator|||
operator|!
name|response
operator|.
name|hasScanMetrics
argument_list|()
operator|||
name|response
operator|.
name|getScanMetrics
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
name|metricMap
return|;
block|}
name|ScanMetrics
name|metrics
init|=
name|response
operator|.
name|getScanMetrics
argument_list|()
decl_stmt|;
name|int
name|numberOfMetrics
init|=
name|metrics
operator|.
name|getMetricsCount
argument_list|()
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
name|numberOfMetrics
condition|;
name|i
operator|++
control|)
block|{
name|NameInt64Pair
name|metricPair
init|=
name|metrics
operator|.
name|getMetrics
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|metricPair
operator|!=
literal|null
condition|)
block|{
name|String
name|name
init|=
name|metricPair
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Long
name|value
init|=
name|metricPair
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|!=
literal|null
operator|&&
name|value
operator|!=
literal|null
condition|)
block|{
name|metricMap
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|metricMap
return|;
block|}
block|}
end_class

end_unit

