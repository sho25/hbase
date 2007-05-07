begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|Text
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
name|ipc
operator|.
name|VersionedProtocol
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

begin_comment
comment|/**  * Clients interact with the HMasterInterface to gain access to meta-level  * HBase functionality, like finding an HRegionServer and creating/destroying  * tables.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HMasterInterface
extends|extends
name|VersionedProtocol
block|{
specifier|public
specifier|static
specifier|final
name|long
name|versionID
init|=
literal|1L
decl_stmt|;
comment|// initial version
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Admin tools would use these cmds
comment|//////////////////////////////////////////////////////////////////////////////
specifier|public
name|void
name|createTable
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|deleteTable
parameter_list|(
name|Text
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Shutdown an HBase cluster.    */
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|//////////////////////////////////////////////////////////////////////////////
comment|// These are the method calls of last resort when trying to find an HRegion
comment|//////////////////////////////////////////////////////////////////////////////
specifier|public
name|HServerAddress
name|findRootRegion
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

