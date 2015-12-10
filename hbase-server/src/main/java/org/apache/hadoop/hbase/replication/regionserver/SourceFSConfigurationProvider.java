begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license  * agreements. See the NOTICE file distributed with this work for additional information regarding  * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License. You may obtain a  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable  * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  * for the specific language governing permissions and limitations under the License.  */
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
name|replication
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
name|HBaseInterfaceAudience
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

begin_comment
comment|/**  * Interface that defines how a region server in peer cluster will get source cluster file system  * configurations. User can configure their custom implementation implementing this interface by  * setting the value of their custom implementation's fully qualified class name to  * hbase.replication.source.fs.conf.provider property in RegionServer configuration. Default is  * {@link DefaultSourceFSConfigurationProvider}  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|REPLICATION
argument_list|)
specifier|public
interface|interface
name|SourceFSConfigurationProvider
block|{
comment|/**    * Returns the source cluster file system configuration for the given source cluster replication    * ID.    * @param sinkConf sink cluster configuration    * @param replicationClusterId unique ID which identifies the source cluster    * @return source cluster file system configuration    * @throws IOException for invalid directory or for a bad disk.    */
specifier|public
name|Configuration
name|getConf
parameter_list|(
name|Configuration
name|sinkConf
parameter_list|,
name|String
name|replicationClusterId
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

