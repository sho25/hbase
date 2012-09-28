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
name|master
operator|.
name|cleaner
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|FileStatus
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
name|HBaseTestingUtility
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
name|ServerName
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
name|SmallTests
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
name|backup
operator|.
name|HFileArchiver
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
name|catalog
operator|.
name|CatalogTracker
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
name|HRegionInfo
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
name|HFileLink
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
name|FSUtils
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
name|HFileArchiveUtil
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * Test the HFileLink Cleaner.  * HFiles with links cannot be deleted until a link is present.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestHFileLinkCleaner
block|{
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testHFileLinkCleaning
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|,
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HFileCleaner
operator|.
name|MASTER_HFILE_CLEANER_PLUGINS
argument_list|,
literal|"org.apache.hadoop.hbase.master.cleaner.TimeToLiveHFileCleaner,"
operator|+
literal|"org.apache.hadoop.hbase.master.cleaner.HFileLinkCleaner"
argument_list|)
expr_stmt|;
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
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
specifier|final
name|String
name|tableName
init|=
literal|"test-table"
decl_stmt|;
specifier|final
name|String
name|tableLinkName
init|=
literal|"test-link"
decl_stmt|;
specifier|final
name|String
name|hfileName
init|=
literal|"1234567890"
decl_stmt|;
specifier|final
name|String
name|familyName
init|=
literal|"cf"
decl_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hriLink
init|=
operator|new
name|HRegionInfo
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableLinkName
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|archiveDir
init|=
name|HFileArchiveUtil
operator|.
name|getArchivePath
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|archiveStoreDir
init|=
name|HFileArchiveUtil
operator|.
name|getStoreArchivePath
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|familyName
argument_list|)
decl_stmt|;
name|Path
name|archiveLinkStoreDir
init|=
name|HFileArchiveUtil
operator|.
name|getStoreArchivePath
argument_list|(
name|conf
argument_list|,
name|tableLinkName
argument_list|,
name|hriLink
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|familyName
argument_list|)
decl_stmt|;
comment|// Create hfile /hbase/table-link/region/cf/getEncodedName.HFILE(conf);
name|Path
name|familyPath
init|=
name|getFamilyDirPath
argument_list|(
name|archiveDir
argument_list|,
name|tableName
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|familyName
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|familyPath
argument_list|)
expr_stmt|;
name|Path
name|hfilePath
init|=
operator|new
name|Path
argument_list|(
name|familyPath
argument_list|,
name|hfileName
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|hfilePath
argument_list|)
expr_stmt|;
comment|// Create link to hfile
name|Path
name|familyLinkPath
init|=
name|getFamilyDirPath
argument_list|(
name|rootDir
argument_list|,
name|tableLinkName
argument_list|,
name|hriLink
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|familyName
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|familyLinkPath
argument_list|)
expr_stmt|;
name|HFileLink
operator|.
name|create
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|familyLinkPath
argument_list|,
name|hri
argument_list|,
name|hfileName
argument_list|)
expr_stmt|;
name|Path
name|linkBackRefDir
init|=
name|HFileLink
operator|.
name|getBackReferencesDir
argument_list|(
name|archiveStoreDir
argument_list|,
name|hfileName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|linkBackRefDir
argument_list|)
argument_list|)
expr_stmt|;
name|FileStatus
index|[]
name|backRefs
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|linkBackRefDir
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|backRefs
operator|.
name|length
argument_list|)
expr_stmt|;
name|Path
name|linkBackRef
init|=
name|backRefs
index|[
literal|0
index|]
operator|.
name|getPath
argument_list|()
decl_stmt|;
comment|// Initialize cleaner
specifier|final
name|long
name|ttl
init|=
literal|1000
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|TimeToLiveHFileCleaner
operator|.
name|TTL_CONF_KEY
argument_list|,
name|ttl
argument_list|)
expr_stmt|;
name|Server
name|server
init|=
operator|new
name|DummyServer
argument_list|()
decl_stmt|;
name|HFileCleaner
name|cleaner
init|=
operator|new
name|HFileCleaner
argument_list|(
literal|1000
argument_list|,
name|server
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|archiveDir
argument_list|)
decl_stmt|;
comment|// Link backref cannot be removed
name|Thread
operator|.
name|sleep
argument_list|(
name|ttl
operator|*
literal|2
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|linkBackRef
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|hfilePath
argument_list|)
argument_list|)
expr_stmt|;
comment|// Link backref can be removed
name|fs
operator|.
name|rename
argument_list|(
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|tableLinkName
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|archiveDir
argument_list|,
name|tableLinkName
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|ttl
operator|*
literal|2
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Link should be deleted"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|linkBackRef
argument_list|)
argument_list|)
expr_stmt|;
comment|// HFile can be removed
name|Thread
operator|.
name|sleep
argument_list|(
name|ttl
operator|*
literal|2
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
literal|"HFile should be deleted"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|hfilePath
argument_list|)
argument_list|)
expr_stmt|;
comment|// Remove everything
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|4
condition|;
operator|++
name|i
control|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|ttl
operator|*
literal|2
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|chore
argument_list|()
expr_stmt|;
block|}
name|assertFalse
argument_list|(
literal|"HFile should be deleted"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
name|archiveDir
argument_list|,
name|tableName
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Link should be deleted"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
name|archiveDir
argument_list|,
name|tableLinkName
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|cleaner
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|Path
name|getFamilyDirPath
parameter_list|(
specifier|final
name|Path
name|rootDir
parameter_list|,
specifier|final
name|String
name|table
parameter_list|,
specifier|final
name|String
name|region
parameter_list|,
specifier|final
name|String
name|family
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|table
argument_list|)
argument_list|,
name|region
argument_list|)
argument_list|,
name|family
argument_list|)
return|;
block|}
specifier|static
class|class
name|DummyServer
implements|implements
name|Server
block|{
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZooKeeperWatcher
name|getZooKeeper
parameter_list|()
block|{
try|try
block|{
return|return
operator|new
name|ZooKeeperWatcher
argument_list|(
name|getConfiguration
argument_list|()
argument_list|,
literal|"dummy server"
argument_list|,
name|this
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CatalogTracker
name|getCatalogTracker
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
operator|new
name|ServerName
argument_list|(
literal|"regionserver,60020,000000"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

