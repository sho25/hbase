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
name|regionserver
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|Server
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
name|replication
operator|.
name|regionserver
operator|.
name|ReplicationLoad
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
name|wal
operator|.
name|WALProvider
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
comment|/**  * Gateway to Cluster Replication. Used by  * {@link org.apache.hadoop.hbase.regionserver.HRegionServer}. One such application is a  * cross-datacenter replication service that can keep two hbase clusters in sync.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ReplicationService
block|{
comment|/**    * Initializes the replication service object.    * @param walProvider can be null if not initialized inside a live region server environment, for    *          example, {@code ReplicationSyncUp}.    */
name|void
name|initialize
parameter_list|(
name|Server
name|rs
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|logdir
parameter_list|,
name|Path
name|oldLogDir
parameter_list|,
name|WALProvider
name|walProvider
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Start replication services.    */
name|void
name|startReplicationService
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Stops replication service.    */
name|void
name|stopReplicationService
parameter_list|()
function_decl|;
comment|/**    * Refresh and Get ReplicationLoad    */
name|ReplicationLoad
name|refreshAndGetReplicationLoad
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

