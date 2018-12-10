begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|ReplicationLoadSink
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
name|ReplicationLoadSource
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
comment|/**  * This class is used for exporting current state of load on a RegionServer.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|ServerMetrics
block|{
name|ServerName
name|getServerName
parameter_list|()
function_decl|;
comment|/**    * @return the version number of a regionserver.    */
specifier|default
name|int
name|getVersionNumber
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
comment|/**    * @return the string type version of a regionserver.    */
specifier|default
name|String
name|getVersion
parameter_list|()
block|{
return|return
literal|"0.0.0"
return|;
block|}
comment|/**    * @return the number of requests per second.    */
name|long
name|getRequestCountPerSecond
parameter_list|()
function_decl|;
comment|/**    * @return total Number of requests from the start of the region server.    */
name|long
name|getRequestCount
parameter_list|()
function_decl|;
comment|/**    * @return the amount of used heap    */
name|Size
name|getUsedHeapSize
parameter_list|()
function_decl|;
comment|/**    * @return the maximum allowable size of the heap    */
name|Size
name|getMaxHeapSize
parameter_list|()
function_decl|;
name|int
name|getInfoServerPort
parameter_list|()
function_decl|;
comment|/**    * Call directly from client such as hbase shell    * @return the list of ReplicationLoadSource    */
name|List
argument_list|<
name|ReplicationLoadSource
argument_list|>
name|getReplicationLoadSourceList
parameter_list|()
function_decl|;
comment|/**    * Call directly from client such as hbase shell    * @return a map of ReplicationLoadSource list per peer id    */
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ReplicationLoadSource
argument_list|>
argument_list|>
name|getReplicationLoadSourceMap
parameter_list|()
function_decl|;
comment|/**    * Call directly from client such as hbase shell    * @return ReplicationLoadSink    */
annotation|@
name|Nullable
name|ReplicationLoadSink
name|getReplicationLoadSink
parameter_list|()
function_decl|;
comment|/**    * @return region load metrics    */
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionMetrics
argument_list|>
name|getRegionMetrics
parameter_list|()
function_decl|;
comment|/**    * Return the RegionServer-level and Region-level coprocessors    * @return string set of loaded RegionServer-level and Region-level coprocessors    */
name|Set
argument_list|<
name|String
argument_list|>
name|getCoprocessorNames
parameter_list|()
function_decl|;
comment|/**    * @return the timestamp (server side) of generating this metrics    */
name|long
name|getReportTimestamp
parameter_list|()
function_decl|;
comment|/**    * @return the last timestamp (server side) of generating this metrics    */
name|long
name|getLastReportTimestamp
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

