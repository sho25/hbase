begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|visibility
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
name|security
operator|.
name|visibility
operator|.
name|VisibilityConstants
operator|.
name|LABELS_TABLE_NAME
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
name|Map
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
name|HBaseZeroCopyByteString
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
name|classification
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
name|ipc
operator|.
name|BlockingRpcCallback
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
name|protobuf
operator|.
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|GetAuthsRequest
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
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|GetAuthsResponse
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
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|SetAuthsRequest
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
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|VisibilityLabel
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
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|VisibilityLabelsRequest
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
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|VisibilityLabelsResponse
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
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|VisibilityLabelsService
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
import|;
end_import

begin_comment
comment|/**  * Utility client for doing visibility labels admin operations.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|VisibilityClient
block|{
comment|/**    * Utility method for adding label to the system.    *     * @param conf    * @param label    * @return VisibilityLabelsResponse    * @throws Throwable    */
specifier|public
specifier|static
name|VisibilityLabelsResponse
name|addLabel
parameter_list|(
name|Configuration
name|conf
parameter_list|,
specifier|final
name|String
name|label
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
name|addLabels
argument_list|(
name|conf
argument_list|,
operator|new
name|String
index|[]
block|{
name|label
block|}
argument_list|)
return|;
block|}
comment|/**    * Utility method for adding labels to the system.    *     * @param conf    * @param labels    * @return VisibilityLabelsResponse    * @throws Throwable    */
specifier|public
specifier|static
name|VisibilityLabelsResponse
name|addLabels
parameter_list|(
name|Configuration
name|conf
parameter_list|,
specifier|final
name|String
index|[]
name|labels
parameter_list|)
throws|throws
name|Throwable
block|{
name|HTable
name|ht
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ht
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|LABELS_TABLE_NAME
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Batch
operator|.
name|Call
argument_list|<
name|VisibilityLabelsService
argument_list|,
name|VisibilityLabelsResponse
argument_list|>
name|callable
init|=
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|VisibilityLabelsService
argument_list|,
name|VisibilityLabelsResponse
argument_list|>
argument_list|()
block|{
name|ServerRpcController
name|controller
init|=
operator|new
name|ServerRpcController
argument_list|()
decl_stmt|;
name|BlockingRpcCallback
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
name|rpcCallback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|VisibilityLabelsResponse
name|call
parameter_list|(
name|VisibilityLabelsService
name|service
parameter_list|)
throws|throws
name|IOException
block|{
name|VisibilityLabelsRequest
operator|.
name|Builder
name|builder
init|=
name|VisibilityLabelsRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|label
range|:
name|labels
control|)
block|{
if|if
condition|(
name|label
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|VisibilityLabel
operator|.
name|Builder
name|newBuilder
init|=
name|VisibilityLabel
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|newBuilder
operator|.
name|setLabel
argument_list|(
name|HBaseZeroCopyByteString
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|label
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addVisLabel
argument_list|(
name|newBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|service
operator|.
name|addLabels
argument_list|(
name|controller
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|rpcCallback
argument_list|)
expr_stmt|;
return|return
name|rpcCallback
operator|.
name|get
argument_list|()
return|;
block|}
block|}
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|VisibilityLabelsResponse
argument_list|>
name|result
init|=
name|ht
operator|.
name|coprocessorService
argument_list|(
name|VisibilityLabelsService
operator|.
name|class
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|callable
argument_list|)
decl_stmt|;
return|return
name|result
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
return|;
comment|// There will be exactly one region for labels
comment|// table and so one entry in result Map.
block|}
finally|finally
block|{
if|if
condition|(
name|ht
operator|!=
literal|null
condition|)
block|{
name|ht
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Sets given labels globally authorized for the user.    * @param conf    * @param auths    * @param user    * @return VisibilityLabelsResponse    * @throws Throwable    */
specifier|public
specifier|static
name|VisibilityLabelsResponse
name|setAuths
parameter_list|(
name|Configuration
name|conf
parameter_list|,
specifier|final
name|String
index|[]
name|auths
parameter_list|,
specifier|final
name|String
name|user
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
name|setOrClearAuths
argument_list|(
name|conf
argument_list|,
name|auths
argument_list|,
name|user
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * @param conf    * @param user    * @return labels, the given user is globally authorized for.    * @throws Throwable    */
specifier|public
specifier|static
name|GetAuthsResponse
name|getAuths
parameter_list|(
name|Configuration
name|conf
parameter_list|,
specifier|final
name|String
name|user
parameter_list|)
throws|throws
name|Throwable
block|{
name|HTable
name|ht
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ht
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|LABELS_TABLE_NAME
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Batch
operator|.
name|Call
argument_list|<
name|VisibilityLabelsService
argument_list|,
name|GetAuthsResponse
argument_list|>
name|callable
init|=
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|VisibilityLabelsService
argument_list|,
name|GetAuthsResponse
argument_list|>
argument_list|()
block|{
name|ServerRpcController
name|controller
init|=
operator|new
name|ServerRpcController
argument_list|()
decl_stmt|;
name|BlockingRpcCallback
argument_list|<
name|GetAuthsResponse
argument_list|>
name|rpcCallback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<
name|GetAuthsResponse
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|GetAuthsResponse
name|call
parameter_list|(
name|VisibilityLabelsService
name|service
parameter_list|)
throws|throws
name|IOException
block|{
name|GetAuthsRequest
operator|.
name|Builder
name|getAuthReqBuilder
init|=
name|GetAuthsRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|getAuthReqBuilder
operator|.
name|setUser
argument_list|(
name|HBaseZeroCopyByteString
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|user
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|service
operator|.
name|getAuths
argument_list|(
name|controller
argument_list|,
name|getAuthReqBuilder
operator|.
name|build
argument_list|()
argument_list|,
name|rpcCallback
argument_list|)
expr_stmt|;
return|return
name|rpcCallback
operator|.
name|get
argument_list|()
return|;
block|}
block|}
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|GetAuthsResponse
argument_list|>
name|result
init|=
name|ht
operator|.
name|coprocessorService
argument_list|(
name|VisibilityLabelsService
operator|.
name|class
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|callable
argument_list|)
decl_stmt|;
return|return
name|result
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
return|;
comment|// There will be exactly one region for labels
comment|// table and so one entry in result Map.
block|}
finally|finally
block|{
if|if
condition|(
name|ht
operator|!=
literal|null
condition|)
block|{
name|ht
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Removes given labels from user's globally authorized list of labels.    * @param conf    * @param auths    * @param user    * @return VisibilityLabelsResponse    * @throws Throwable    */
specifier|public
specifier|static
name|VisibilityLabelsResponse
name|clearAuths
parameter_list|(
name|Configuration
name|conf
parameter_list|,
specifier|final
name|String
index|[]
name|auths
parameter_list|,
specifier|final
name|String
name|user
parameter_list|)
throws|throws
name|Throwable
block|{
return|return
name|setOrClearAuths
argument_list|(
name|conf
argument_list|,
name|auths
argument_list|,
name|user
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|VisibilityLabelsResponse
name|setOrClearAuths
parameter_list|(
name|Configuration
name|conf
parameter_list|,
specifier|final
name|String
index|[]
name|auths
parameter_list|,
specifier|final
name|String
name|user
parameter_list|,
specifier|final
name|boolean
name|setOrClear
parameter_list|)
throws|throws
name|IOException
throws|,
name|ServiceException
throws|,
name|Throwable
block|{
name|HTable
name|ht
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ht
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|LABELS_TABLE_NAME
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Batch
operator|.
name|Call
argument_list|<
name|VisibilityLabelsService
argument_list|,
name|VisibilityLabelsResponse
argument_list|>
name|callable
init|=
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|VisibilityLabelsService
argument_list|,
name|VisibilityLabelsResponse
argument_list|>
argument_list|()
block|{
name|ServerRpcController
name|controller
init|=
operator|new
name|ServerRpcController
argument_list|()
decl_stmt|;
name|BlockingRpcCallback
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
name|rpcCallback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|VisibilityLabelsResponse
name|call
parameter_list|(
name|VisibilityLabelsService
name|service
parameter_list|)
throws|throws
name|IOException
block|{
name|SetAuthsRequest
operator|.
name|Builder
name|setAuthReqBuilder
init|=
name|SetAuthsRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|setAuthReqBuilder
operator|.
name|setUser
argument_list|(
name|HBaseZeroCopyByteString
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|user
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|auth
range|:
name|auths
control|)
block|{
if|if
condition|(
name|auth
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|setAuthReqBuilder
operator|.
name|addAuth
argument_list|(
name|HBaseZeroCopyByteString
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|auth
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|setOrClear
condition|)
block|{
name|service
operator|.
name|setAuths
argument_list|(
name|controller
argument_list|,
name|setAuthReqBuilder
operator|.
name|build
argument_list|()
argument_list|,
name|rpcCallback
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|service
operator|.
name|clearAuths
argument_list|(
name|controller
argument_list|,
name|setAuthReqBuilder
operator|.
name|build
argument_list|()
argument_list|,
name|rpcCallback
argument_list|)
expr_stmt|;
block|}
return|return
name|rpcCallback
operator|.
name|get
argument_list|()
return|;
block|}
block|}
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|VisibilityLabelsResponse
argument_list|>
name|result
init|=
name|ht
operator|.
name|coprocessorService
argument_list|(
name|VisibilityLabelsService
operator|.
name|class
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|callable
argument_list|)
decl_stmt|;
return|return
name|result
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
return|;
comment|// There will be exactly one region for labels
comment|// table and so one entry in result Map.
block|}
finally|finally
block|{
if|if
condition|(
name|ht
operator|!=
literal|null
condition|)
block|{
name|ht
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

