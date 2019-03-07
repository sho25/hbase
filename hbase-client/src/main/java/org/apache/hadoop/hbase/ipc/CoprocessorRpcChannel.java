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
name|ipc
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
name|BlockingRpcChannel
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
name|RpcChannel
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

begin_comment
comment|/**  * Base interface which provides clients with an RPC connection to call coprocessor endpoint  * {@link com.google.protobuf.Service}s.  *<p/>  * Note that clients should not use this class directly, except through  * {@link org.apache.hadoop.hbase.client.Table#coprocessorService(byte[])}.  *<p/>  * @deprecated Please stop using this class again, as it is too low level, which is part of the rpc  *             framework for HBase. Will be deleted in 4.0.0.  */
end_comment

begin_interface
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|CoprocessorRpcChannel
extends|extends
name|RpcChannel
extends|,
name|BlockingRpcChannel
block|{ }
end_interface

begin_comment
comment|// This Interface is part of our public, client-facing API!!!
end_comment

begin_comment
comment|// This belongs in client package but it is exposed in our public API so we cannot relocate.
end_comment

end_unit

