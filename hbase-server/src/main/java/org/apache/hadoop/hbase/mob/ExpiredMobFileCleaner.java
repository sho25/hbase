begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mob
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|Configured
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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|TableName
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
name|Admin
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
name|Connection
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
name|ConnectionFactory
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
name|HBaseAdmin
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
name|io
operator|.
name|hfile
operator|.
name|CacheConfig
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
name|EnvironmentEdgeManager
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
name|util
operator|.
name|Tool
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
name|util
operator|.
name|ToolRunner
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
comment|/**  * The cleaner to delete the expired MOB files.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ExpiredMobFileCleaner
extends|extends
name|Configured
implements|implements
name|Tool
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ExpiredMobFileCleaner
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Cleans the MOB files when they're expired and their min versions are 0.    * If the latest timestamp of Cells in a MOB file is older than the TTL in the column family,    * it's regarded as expired. This cleaner deletes them.    * At a time T0, the cells in a mob file M0 are expired. If a user starts a scan before T0, those    * mob cells are visible, this scan still runs after T0. At that time T1, this mob file M0    * is expired, meanwhile a cleaner starts, the M0 is archived and can be read in the archive    * directory.    * @param tableName The current table name.    * @param family The current family.    * @throws ServiceException    * @throws IOException    */
specifier|public
name|void
name|cleanExpiredMobFiles
parameter_list|(
name|String
name|tableName
parameter_list|,
name|HColumnDescriptor
name|family
parameter_list|)
throws|throws
name|ServiceException
throws|,
name|IOException
block|{
name|Configuration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Cleaning the expired MOB files of "
operator|+
name|family
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|" in "
operator|+
name|tableName
argument_list|)
expr_stmt|;
comment|// disable the block cache.
name|Configuration
name|copyOfConf
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|copyOfConf
operator|.
name|setFloat
argument_list|(
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_KEY
argument_list|,
literal|0f
argument_list|)
expr_stmt|;
name|CacheConfig
name|cacheConfig
init|=
operator|new
name|CacheConfig
argument_list|(
name|copyOfConf
argument_list|)
decl_stmt|;
name|MobUtils
operator|.
name|cleanExpiredMobFiles
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|tn
argument_list|,
name|family
argument_list|,
name|cacheConfig
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|ExpiredMobFileCleaner
argument_list|()
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|printUsage
parameter_list|()
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Usage:\n"
operator|+
literal|"--------------------------\n"
operator|+
name|ExpiredMobFileCleaner
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|" tableName familyName"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" tableName        The table name"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" familyName       The column family name"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
name|printUsage
argument_list|()
expr_stmt|;
return|return
literal|1
return|;
block|}
name|String
name|tableName
init|=
name|args
index|[
literal|0
index|]
decl_stmt|;
name|String
name|familyName
init|=
name|args
index|[
literal|1
index|]
decl_stmt|;
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HBaseAdmin
operator|.
name|checkHBaseAvailable
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|Admin
name|admin
init|=
name|connection
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
try|try
block|{
name|HTableDescriptor
name|htd
init|=
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|tn
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|family
init|=
name|htd
operator|.
name|getFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|familyName
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|family
operator|==
literal|null
operator|||
operator|!
name|family
operator|.
name|isMobEnabled
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Column family "
operator|+
name|familyName
operator|+
literal|" is not a MOB column family"
argument_list|)
throw|;
block|}
if|if
condition|(
name|family
operator|.
name|getMinVersions
argument_list|()
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"The minVersions of the column family is not 0, could not be handled by this cleaner"
argument_list|)
throw|;
block|}
name|cleanExpiredMobFiles
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
finally|finally
block|{
try|try
block|{
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to close the HBaseAdmin."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to close the connection."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

