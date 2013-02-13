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
name|snapshot
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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|fs
operator|.
name|PathFilter
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
name|master
operator|.
name|HMaster
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|SnapshotDescription
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
name|protobuf
operator|.
name|generated
operator|.
name|MasterAdminProtos
operator|.
name|IsSnapshotDoneRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|MasterAdminProtos
operator|.
name|IsSnapshotDoneResponse
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
name|regionserver
operator|.
name|HRegion
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
name|FSTableDescriptors
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
name|junit
operator|.
name|Assert
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
comment|/**  * Utilities class for snapshots  */
end_comment

begin_class
specifier|public
class|class
name|SnapshotTestingUtils
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
name|SnapshotTestingUtils
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Assert that we don't have any snapshots lists    * @throws IOException if the admin operation fails    */
specifier|public
specifier|static
name|void
name|assertNoSnapshots
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|)
throws|throws
name|IOException
block|{
name|assertEquals
argument_list|(
literal|"Have some previous snapshots"
argument_list|,
literal|0
argument_list|,
name|admin
operator|.
name|listSnapshots
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Make sure that there is only one snapshot returned from the master and its name and table match    * the passed in parameters.    */
specifier|public
specifier|static
name|void
name|assertOneSnapshotThatMatches
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|SnapshotDescription
name|snapshot
parameter_list|)
throws|throws
name|IOException
block|{
name|assertOneSnapshotThatMatches
argument_list|(
name|admin
argument_list|,
name|snapshot
operator|.
name|getName
argument_list|()
argument_list|,
name|snapshot
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Make sure that there is only one snapshot returned from the master and its name and table match    * the passed in parameters.    */
specifier|public
specifier|static
name|List
argument_list|<
name|SnapshotDescription
argument_list|>
name|assertOneSnapshotThatMatches
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// list the snapshot
name|List
argument_list|<
name|SnapshotDescription
argument_list|>
name|snapshots
init|=
name|admin
operator|.
name|listSnapshots
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Should only have 1 snapshot"
argument_list|,
literal|1
argument_list|,
name|snapshots
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|snapshotName
argument_list|,
name|snapshots
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
argument_list|,
name|snapshots
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|snapshots
return|;
block|}
comment|/**    * Make sure that there is only one snapshot returned from the master and its name and table match    * the passed in parameters.    */
specifier|public
specifier|static
name|List
argument_list|<
name|SnapshotDescription
argument_list|>
name|assertOneSnapshotThatMatches
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|byte
index|[]
name|snapshot
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|assertOneSnapshotThatMatches
argument_list|(
name|admin
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Confirm that the snapshot contains references to all the files that should be in the snapshot    */
specifier|public
specifier|static
name|void
name|confirmSnapshotValid
parameter_list|(
name|SnapshotDescription
name|snapshotDescriptor
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|testFamily
parameter_list|,
name|Path
name|rootDir
parameter_list|,
name|HBaseAdmin
name|admin
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|boolean
name|requireLogs
parameter_list|,
name|Path
name|logsDir
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|snapshotServers
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|snapshotDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getCompletedSnapshotDir
argument_list|(
name|snapshotDescriptor
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|snapshotDir
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|snapshotinfo
init|=
operator|new
name|Path
argument_list|(
name|snapshotDir
argument_list|,
name|SnapshotDescriptionUtils
operator|.
name|SNAPSHOTINFO_FILE
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|snapshotinfo
argument_list|)
argument_list|)
expr_stmt|;
comment|// check the logs dir
if|if
condition|(
name|requireLogs
condition|)
block|{
name|TakeSnapshotUtils
operator|.
name|verifyAllLogsGotReferenced
argument_list|(
name|fs
argument_list|,
name|logsDir
argument_list|,
name|snapshotServers
argument_list|,
name|snapshotDescriptor
argument_list|,
operator|new
name|Path
argument_list|(
name|snapshotDir
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// check the table info
name|HTableDescriptor
name|desc
init|=
name|FSTableDescriptors
operator|.
name|getTableDescriptor
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|snapshotDesc
init|=
name|FSTableDescriptors
operator|.
name|getTableDescriptor
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|desc
argument_list|,
name|snapshotDesc
argument_list|)
expr_stmt|;
comment|// check the region snapshot for all the regions
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|info
range|:
name|regions
control|)
block|{
name|String
name|regionName
init|=
name|info
operator|.
name|getEncodedName
argument_list|()
decl_stmt|;
name|Path
name|regionDir
init|=
operator|new
name|Path
argument_list|(
name|snapshotDir
argument_list|,
name|regionName
argument_list|)
decl_stmt|;
name|HRegionInfo
name|snapshotRegionInfo
init|=
name|HRegion
operator|.
name|loadDotRegionInfoFileContent
argument_list|(
name|fs
argument_list|,
name|regionDir
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|info
argument_list|,
name|snapshotRegionInfo
argument_list|)
expr_stmt|;
comment|// check to make sure we have the family
name|Path
name|familyDir
init|=
operator|new
name|Path
argument_list|(
name|regionDir
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|testFamily
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Expected to find: "
operator|+
name|familyDir
operator|+
literal|", but it doesn't exist"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|familyDir
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure we have some files references
name|assertTrue
argument_list|(
name|fs
operator|.
name|listStatus
argument_list|(
name|familyDir
argument_list|)
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Helper method for testing async snapshot operations. Just waits for the given snapshot to    * complete on the server by repeatedly checking the master.    * @param master running the snapshot    * @param snapshot to check    * @param sleep amount to sleep between checks to see if the snapshot is done    * @throws ServiceException if the snapshot fails    */
specifier|public
specifier|static
name|void
name|waitForSnapshotToComplete
parameter_list|(
name|HMaster
name|master
parameter_list|,
name|SnapshotDescription
name|snapshot
parameter_list|,
name|long
name|sleep
parameter_list|)
throws|throws
name|ServiceException
block|{
specifier|final
name|IsSnapshotDoneRequest
name|request
init|=
name|IsSnapshotDoneRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setSnapshot
argument_list|(
name|snapshot
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IsSnapshotDoneResponse
name|done
init|=
name|IsSnapshotDoneResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|buildPartial
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|done
operator|.
name|getDone
argument_list|()
condition|)
block|{
name|done
operator|=
name|master
operator|.
name|isSnapshotDone
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleep
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
name|void
name|cleanupSnapshot
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|SnapshotTestingUtils
operator|.
name|cleanupSnapshot
argument_list|(
name|admin
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|cleanupSnapshot
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|String
name|snapshotName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// delete the taken snapshot
name|admin
operator|.
name|deleteSnapshot
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
name|assertNoSnapshots
argument_list|(
name|admin
argument_list|)
expr_stmt|;
block|}
comment|/**    * Expect the snapshot to throw an error when checking if the snapshot is complete    * @param master master to check    * @param snapshot the {@link SnapshotDescription} request to pass to the master    * @param clazz expected exception from the master    */
specifier|public
specifier|static
name|void
name|expectSnapshotDoneException
parameter_list|(
name|HMaster
name|master
parameter_list|,
name|IsSnapshotDoneRequest
name|snapshot
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|HBaseSnapshotException
argument_list|>
name|clazz
parameter_list|)
block|{
try|try
block|{
name|master
operator|.
name|isSnapshotDone
argument_list|(
literal|null
argument_list|,
name|snapshot
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"didn't fail to lookup a snapshot"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|se
parameter_list|)
block|{
try|try
block|{
throw|throw
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|se
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|HBaseSnapshotException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Threw wrong snapshot exception!"
argument_list|,
name|clazz
argument_list|,
name|e
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Threw an unexpected exception:"
operator|+
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * List all the HFiles in the given table    * @param fs FileSystem where the table lives    * @param tableDir directory of the table    * @return array of the current HFiles in the table (could be a zero-length array)    * @throws IOException on unexecpted error reading the FS    */
specifier|public
specifier|static
name|FileStatus
index|[]
name|listHFiles
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
name|Path
name|tableDir
parameter_list|)
throws|throws
name|IOException
block|{
comment|// setup the filters we will need based on the filesystem
name|PathFilter
name|regionFilter
init|=
operator|new
name|FSUtils
operator|.
name|RegionDirFilter
argument_list|(
name|fs
argument_list|)
decl_stmt|;
name|PathFilter
name|familyFilter
init|=
operator|new
name|FSUtils
operator|.
name|FamilyDirFilter
argument_list|(
name|fs
argument_list|)
decl_stmt|;
specifier|final
name|PathFilter
name|fileFilter
init|=
operator|new
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
try|try
block|{
return|return
name|fs
operator|.
name|isFile
argument_list|(
name|file
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
decl_stmt|;
name|FileStatus
index|[]
name|regionDirs
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|tableDir
argument_list|,
name|regionFilter
argument_list|)
decl_stmt|;
comment|// if no regions, then we are done
if|if
condition|(
name|regionDirs
operator|==
literal|null
operator|||
name|regionDirs
operator|.
name|length
operator|==
literal|0
condition|)
return|return
operator|new
name|FileStatus
index|[
literal|0
index|]
return|;
comment|// go through each of the regions, and add al the hfiles under each family
name|List
argument_list|<
name|FileStatus
argument_list|>
name|regionFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
argument_list|(
name|regionDirs
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|regionDir
range|:
name|regionDirs
control|)
block|{
name|FileStatus
index|[]
name|fams
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|regionDir
operator|.
name|getPath
argument_list|()
argument_list|,
name|familyFilter
argument_list|)
decl_stmt|;
comment|// if no families, then we are done again
if|if
condition|(
name|fams
operator|==
literal|null
operator|||
name|fams
operator|.
name|length
operator|==
literal|0
condition|)
continue|continue;
comment|// add all the hfiles under the family
name|regionFiles
operator|.
name|addAll
argument_list|(
name|SnapshotTestingUtils
operator|.
name|getHFilesInRegion
argument_list|(
name|fams
argument_list|,
name|fs
argument_list|,
name|fileFilter
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|FileStatus
index|[]
name|files
init|=
operator|new
name|FileStatus
index|[
name|regionFiles
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|regionFiles
operator|.
name|toArray
argument_list|(
name|files
argument_list|)
expr_stmt|;
return|return
name|files
return|;
block|}
comment|/**    * Get all the hfiles in the region, under the passed set of families    * @param families all the family directories under the region    * @param fs filesystem where the families live    * @param fileFilter filter to only include files    * @return collection of all the hfiles under all the passed in families (non-null)    * @throws IOException on unexecpted error reading the FS    */
specifier|public
specifier|static
name|Collection
argument_list|<
name|FileStatus
argument_list|>
name|getHFilesInRegion
parameter_list|(
name|FileStatus
index|[]
name|families
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|PathFilter
name|fileFilter
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|FileStatus
argument_list|>
name|files
init|=
operator|new
name|TreeSet
argument_list|<
name|FileStatus
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|family
range|:
name|families
control|)
block|{
comment|// get all the hfiles in the family
name|FileStatus
index|[]
name|hfiles
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|family
operator|.
name|getPath
argument_list|()
argument_list|,
name|fileFilter
argument_list|)
decl_stmt|;
comment|// if no hfiles, then we are done with this family
if|if
condition|(
name|hfiles
operator|==
literal|null
operator|||
name|hfiles
operator|.
name|length
operator|==
literal|0
condition|)
continue|continue;
name|files
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|hfiles
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|files
return|;
block|}
block|}
end_class

end_unit

