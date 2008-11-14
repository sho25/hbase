begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * There is one version id for all the RPC interfaces. If any interface  * is changed, the versionID must be changed here.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HBaseRPCProtocolVersion
extends|extends
name|VersionedProtocol
block|{
comment|/**    * Interface version.    *     * HMasterInterface version history:    *<ul>    *<li>Version was incremented to 2 when we brought the hadoop RPC local to    * hbase HADOOP-2495</li>    *<li>Version was incremented to 3 when we changed the RPC to send codes    * instead of actual class names (HADOOP-2519).</li>    *<li>Version 4 when we moved to all byte arrays (HBASE-42).</li>    *<li>Version 5  HBASE-576.</li>    *<li>Version 6  modifyTable.</li>    *</ul>    *<p>HMasterRegionInterface version history:    *<ul>    *<li>Version 2 was when the regionServerStartup was changed to return a    * MapWritable instead of a HbaseMapWritable as part of HBASE-82 changes.</li>    *<li>Version 3 was when HMsg was refactored so it could carry optional    * messages (HBASE-504).</li>    *<li>HBASE-576 we moved this to 4.</li>    *</ul>    *<p>HRegionInterface version history:    *<ul>    *<li>Upped to 5 when we added scanner caching</li>    *<li>HBASE-576, we moved this to 6.</li>    *</ul>    *<p>TransactionalRegionInterface version history:    *<ul>    *<li>Moved to 2 for hbase-576.</li>    *</ul>    *<p>Unified RPC version number history:    *<ul>    *<li>Version 10: initial version (had to be&gt all other RPC versions</li>    *<li>Version 11: Changed getClosestRowBefore signature.    *</ul>    */
specifier|public
specifier|static
specifier|final
name|long
name|versionID
init|=
literal|11L
decl_stmt|;
block|}
end_interface

end_unit

