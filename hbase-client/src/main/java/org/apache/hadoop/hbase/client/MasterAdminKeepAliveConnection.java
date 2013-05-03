begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|MasterAdminProtos
import|;
end_import

begin_comment
comment|/**  * A KeepAlive connection is not physically closed immediately after the close,  *  but rather kept alive for a few minutes. It makes sense only if it is shared.  *  *<p>This interface is implemented on a stub. It allows to have a #close function in a master  * client.  *  *<p>This class is intended to be used internally by HBase classes that need to make invocations  * against the master on the MasterAdminProtos.MasterAdminService.BlockingInterface; but not by  * final user code. Hence it's package protected.  */
end_comment

begin_interface
interface|interface
name|MasterAdminKeepAliveConnection
extends|extends
name|MasterAdminProtos
operator|.
name|MasterAdminService
operator|.
name|BlockingInterface
block|{
comment|/**    * Close down all resources.    */
comment|// The Closeable Interface wants to throw an IOE out of a close.
comment|//  Thats a PITA.  Do this below instead of Closeable.
specifier|public
name|void
name|close
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

