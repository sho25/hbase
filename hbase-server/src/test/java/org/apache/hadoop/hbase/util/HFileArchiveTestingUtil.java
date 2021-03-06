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
name|util
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
name|assertNull
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
name|Collections
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
name|regionserver
operator|.
name|Store
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
comment|/**  * Test helper for testing archiving of HFiles  */
end_comment

begin_class
specifier|public
class|class
name|HFileArchiveTestingUtil
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
name|HFileArchiveTestingUtil
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HFileArchiveTestingUtil
parameter_list|()
block|{
comment|// NOOP private ctor since this is just a utility class
block|}
specifier|public
specifier|static
name|boolean
name|compareArchiveToOriginal
parameter_list|(
name|FileStatus
index|[]
name|previous
parameter_list|,
name|FileStatus
index|[]
name|archived
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|boolean
name|hasTimedBackup
parameter_list|)
block|{
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|lists
init|=
name|getFileLists
argument_list|(
name|previous
argument_list|,
name|archived
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|original
init|=
name|lists
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|original
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|currentFiles
init|=
name|lists
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|currentFiles
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|backedup
init|=
name|lists
operator|.
name|get
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|backedup
argument_list|)
expr_stmt|;
comment|// check the backed up files versus the current (should match up, less the
comment|// backup time in the name)
if|if
condition|(
operator|!
name|hasTimedBackup
operator|==
operator|(
name|backedup
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"backedup files doesn't match expected."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|String
name|msg
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|hasTimedBackup
condition|)
block|{
name|msg
operator|=
name|assertArchiveEquality
argument_list|(
name|original
argument_list|,
name|backedup
argument_list|)
expr_stmt|;
if|if
condition|(
name|msg
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|msg
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
name|msg
operator|=
name|assertArchiveEquality
argument_list|(
name|original
argument_list|,
name|currentFiles
argument_list|)
expr_stmt|;
if|if
condition|(
name|msg
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|msg
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Compare the archived files to the files in the original directory    * @param expected original files that should have been archived    * @param actual files that were archived    * @param fs filessystem on which the archiving took place    * @throws IOException    */
specifier|public
specifier|static
name|void
name|assertArchiveEqualToOriginal
parameter_list|(
name|FileStatus
index|[]
name|expected
parameter_list|,
name|FileStatus
index|[]
name|actual
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
name|assertArchiveEqualToOriginal
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|,
name|fs
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Compare the archived files to the files in the original directory    * @param expected original files that should have been archived    * @param actual files that were archived    * @param fs {@link FileSystem} on which the archiving took place    * @param hasTimedBackup<tt>true</tt> if we expect to find an archive backup directory with a    *          copy of the files in the archive directory (and the original files).    * @throws IOException    */
specifier|public
specifier|static
name|void
name|assertArchiveEqualToOriginal
parameter_list|(
name|FileStatus
index|[]
name|expected
parameter_list|,
name|FileStatus
index|[]
name|actual
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|boolean
name|hasTimedBackup
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|lists
init|=
name|getFileLists
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|original
init|=
name|lists
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|original
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|currentFiles
init|=
name|lists
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|currentFiles
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|backedup
init|=
name|lists
operator|.
name|get
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|backedup
argument_list|)
expr_stmt|;
comment|// check the backed up files versus the current (should match up, less the
comment|// backup time in the name)
name|assertEquals
argument_list|(
literal|"Didn't expect any backup files, but got: "
operator|+
name|backedup
argument_list|,
name|hasTimedBackup
argument_list|,
name|backedup
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|String
name|msg
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|hasTimedBackup
condition|)
block|{
name|assertArchiveEquality
argument_list|(
name|original
argument_list|,
name|backedup
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|msg
argument_list|,
name|msg
argument_list|)
expr_stmt|;
block|}
comment|// do the rest of the comparison
name|msg
operator|=
name|assertArchiveEquality
argument_list|(
name|original
argument_list|,
name|currentFiles
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|msg
argument_list|,
name|msg
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|String
name|assertArchiveEquality
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|expected
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|archived
parameter_list|)
block|{
name|String
name|compare
init|=
name|compareFileLists
argument_list|(
name|expected
argument_list|,
name|archived
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|expected
operator|.
name|size
argument_list|()
operator|==
name|archived
operator|.
name|size
argument_list|()
operator|)
condition|)
return|return
literal|"Not the same number of current files\n"
operator|+
name|compare
return|;
if|if
condition|(
operator|!
name|expected
operator|.
name|equals
argument_list|(
name|archived
argument_list|)
condition|)
return|return
literal|"Different backup files, but same amount\n"
operator|+
name|compare
return|;
return|return
literal|null
return|;
block|}
comment|/**    * @return&lt;expected, gotten, backup&gt;, where each is sorted    */
specifier|private
specifier|static
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getFileLists
parameter_list|(
name|FileStatus
index|[]
name|previous
parameter_list|,
name|FileStatus
index|[]
name|archived
parameter_list|)
block|{
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
comment|// copy over the original files
name|List
argument_list|<
name|String
argument_list|>
name|originalFileNames
init|=
name|convertToString
argument_list|(
name|previous
argument_list|)
decl_stmt|;
name|files
operator|.
name|add
argument_list|(
name|originalFileNames
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|currentFiles
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|previous
operator|.
name|length
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|backedupFiles
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|previous
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|f
range|:
name|archived
control|)
block|{
name|String
name|name
init|=
name|f
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// if the file has been backed up
if|if
condition|(
name|name
operator|.
name|contains
argument_list|(
literal|"."
argument_list|)
condition|)
block|{
name|Path
name|parent
init|=
name|f
operator|.
name|getPath
argument_list|()
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|String
name|shortName
init|=
name|name
operator|.
name|split
argument_list|(
literal|"[.]"
argument_list|)
index|[
literal|0
index|]
decl_stmt|;
name|Path
name|modPath
init|=
operator|new
name|Path
argument_list|(
name|parent
argument_list|,
name|shortName
argument_list|)
decl_stmt|;
name|FileStatus
name|file
init|=
operator|new
name|FileStatus
argument_list|(
name|f
operator|.
name|getLen
argument_list|()
argument_list|,
name|f
operator|.
name|isDirectory
argument_list|()
argument_list|,
name|f
operator|.
name|getReplication
argument_list|()
argument_list|,
name|f
operator|.
name|getBlockSize
argument_list|()
argument_list|,
name|f
operator|.
name|getModificationTime
argument_list|()
argument_list|,
name|modPath
argument_list|)
decl_stmt|;
name|backedupFiles
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// otherwise, add it to the list to compare to the original store files
name|currentFiles
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
name|files
operator|.
name|add
argument_list|(
name|currentFiles
argument_list|)
expr_stmt|;
name|files
operator|.
name|add
argument_list|(
name|convertToString
argument_list|(
name|backedupFiles
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|files
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|convertToString
parameter_list|(
name|FileStatus
index|[]
name|files
parameter_list|)
block|{
return|return
name|convertToString
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|files
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|convertToString
parameter_list|(
name|List
argument_list|<
name|FileStatus
argument_list|>
name|files
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|originalFileNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|files
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|f
range|:
name|files
control|)
block|{
name|originalFileNames
operator|.
name|add
argument_list|(
name|f
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|originalFileNames
return|;
block|}
comment|/* Get a pretty representation of the differences */
specifier|private
specifier|static
name|String
name|compareFileLists
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|expected
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|gotten
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"Expected ("
operator|+
name|expected
operator|.
name|size
argument_list|()
operator|+
literal|"): \t\t Gotten ("
operator|+
name|gotten
operator|.
name|size
argument_list|()
operator|+
literal|"):\n"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|notFound
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|expected
control|)
block|{
if|if
condition|(
name|gotten
operator|.
name|contains
argument_list|(
name|s
argument_list|)
condition|)
name|sb
operator|.
name|append
argument_list|(
name|s
operator|+
literal|"\t\t"
operator|+
name|s
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
else|else
name|notFound
operator|.
name|add
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"Not Found:\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|s
range|:
name|notFound
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|s
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"\nExtra:\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|s
range|:
name|gotten
control|)
block|{
if|if
condition|(
operator|!
name|expected
operator|.
name|contains
argument_list|(
name|s
argument_list|)
condition|)
name|sb
operator|.
name|append
argument_list|(
name|s
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Helper method to get the archive directory for the specified region    * @param conf {@link Configuration} to check for the name of the archive directory    * @param region region that is being archived    * @return {@link Path} to the archive directory for the given region    */
specifier|public
specifier|static
name|Path
name|getRegionArchiveDir
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|HFileArchiveUtil
operator|.
name|getRegionArchiveDir
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|,
name|region
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Helper method to get the store archive directory for the specified region    * @param conf {@link Configuration} to check for the name of the archive directory    * @param region region that is being archived    * @param store store that is archiving files    * @return {@link Path} to the store archive directory for the given region    */
specifier|public
specifier|static
name|Path
name|getStoreArchivePath
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HRegion
name|region
parameter_list|,
name|Store
name|store
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|HFileArchiveUtil
operator|.
name|getStoreArchivePath
argument_list|(
name|conf
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|region
operator|.
name|getRegionFileSystem
argument_list|()
operator|.
name|getTableDir
argument_list|()
argument_list|,
name|store
operator|.
name|getColumnFamilyDescriptor
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Path
name|getStoreArchivePath
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|String
name|tableName
parameter_list|,
name|byte
index|[]
name|storeName
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|table
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// get the RS and region serving our table
name|List
argument_list|<
name|HRegion
argument_list|>
name|servingRegions
init|=
name|util
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|servingRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// check that we actually have some store files that were archived
name|Store
name|store
init|=
name|region
operator|.
name|getStore
argument_list|(
name|storeName
argument_list|)
decl_stmt|;
return|return
name|HFileArchiveTestingUtil
operator|.
name|getStoreArchivePath
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|region
argument_list|,
name|store
argument_list|)
return|;
block|}
block|}
end_class

end_unit

