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
name|File
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
name|net
operator|.
name|URL
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
name|Map
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
name|fs
operator|.
name|FileUtil
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
name|HBaseConfiguration
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * This will load all the xml configuration files for the source cluster replication ID from  * user configured replication configuration directory.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DefaultSourceFSConfigurationProvider
implements|implements
name|SourceFSConfigurationProvider
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|DefaultSourceFSConfigurationProvider
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Map containing all the source clusters configurations against their replication cluster id
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Configuration
argument_list|>
name|sourceClustersConfs
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|XML
init|=
literal|".xml"
decl_stmt|;
annotation|@
name|Override
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
block|{
if|if
condition|(
name|sourceClustersConfs
operator|.
name|get
argument_list|(
name|replicationClusterId
argument_list|)
operator|==
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|sourceClustersConfs
init|)
block|{
if|if
condition|(
name|sourceClustersConfs
operator|.
name|get
argument_list|(
name|replicationClusterId
argument_list|)
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Loading source cluster FS client conf for cluster "
operator|+
name|replicationClusterId
argument_list|)
expr_stmt|;
comment|// Load only user provided client configurations.
name|Configuration
name|sourceClusterConf
init|=
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|String
name|replicationConfDir
init|=
name|sinkConf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|REPLICATION_CONF_DIR
argument_list|)
decl_stmt|;
if|if
condition|(
name|replicationConfDir
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|HConstants
operator|.
name|REPLICATION_CONF_DIR
operator|+
literal|" is not configured."
argument_list|)
expr_stmt|;
name|URL
name|resource
init|=
name|HBaseConfiguration
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
operator|.
name|getResource
argument_list|(
literal|"hbase-site.xml"
argument_list|)
decl_stmt|;
if|if
condition|(
name|resource
operator|!=
literal|null
condition|)
block|{
name|String
name|path
init|=
name|resource
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|replicationConfDir
operator|=
name|path
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|path
operator|.
name|lastIndexOf
argument_list|(
literal|"/"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|replicationConfDir
operator|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HBASE_CONF_DIR"
argument_list|)
expr_stmt|;
block|}
block|}
name|File
name|confDir
init|=
operator|new
name|File
argument_list|(
name|replicationConfDir
argument_list|,
name|replicationClusterId
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Loading source cluster "
operator|+
name|replicationClusterId
operator|+
literal|" file system configurations from xml "
operator|+
literal|"files under directory "
operator|+
name|confDir
argument_list|)
expr_stmt|;
name|String
index|[]
name|listofConfFiles
init|=
name|FileUtil
operator|.
name|list
argument_list|(
name|confDir
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|confFile
range|:
name|listofConfFiles
control|)
block|{
if|if
condition|(
operator|new
name|File
argument_list|(
name|confDir
argument_list|,
name|confFile
argument_list|)
operator|.
name|isFile
argument_list|()
operator|&&
name|confFile
operator|.
name|endsWith
argument_list|(
name|XML
argument_list|)
condition|)
block|{
comment|// Add all the user provided client conf files
name|sourceClusterConf
operator|.
name|addResource
argument_list|(
operator|new
name|Path
argument_list|(
name|confDir
operator|.
name|getPath
argument_list|()
argument_list|,
name|confFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|sourceClustersConfs
operator|.
name|put
argument_list|(
name|replicationClusterId
argument_list|,
name|sourceClusterConf
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|this
operator|.
name|sourceClustersConfs
operator|.
name|get
argument_list|(
name|replicationClusterId
argument_list|)
return|;
block|}
block|}
end_class

end_unit

