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
name|MasterAdminProtocol
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

begin_comment
comment|/**  * A KeepAlive connection is not physically closed immediately after the close,  *  but rather kept alive for a few minutes. It makes sense only if it's shared.  *  * This interface is used by a dynamic proxy. It allows to have a #close  *  function in a master client.  *  * This class is intended to be used internally by HBase classes that need to  * speak the MasterAdminProtocol; but not by * final user code. Hence it's  * package protected.  */
end_comment

begin_interface
interface|interface
name|MasterAdminKeepAliveConnection
extends|extends
name|MasterAdminProtocol
extends|,
name|Closeable
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

