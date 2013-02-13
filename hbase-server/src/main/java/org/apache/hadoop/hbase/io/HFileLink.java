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
name|io
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
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|StoreFile
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

begin_comment
comment|/**  * HFileLink describes a link to an hfile.  *  * An hfile can be served from a region or from the hfile archive directory (/hbase/.archive)  * HFileLink allows to access the referenced hfile regardless of the location where it is.  *  *<p>Searches for hfiles in the following order and locations:  *<ul>  *<li>/hbase/table/region/cf/hfile</li>  *<li>/hbase/.archive/table/region/cf/hfile</li>  *</ul>  *  * The link checks first in the original path if it is not present  * it fallbacks to the archived path.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HFileLink
extends|extends
name|FileLink
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
name|HFileLink
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * A non-capture group, for HFileLink, so that this can be embedded.    * The HFileLink describe a link to an hfile in a different table/region    * and the name is in the form: table=region-hfile.    *<p>    * Table name is ([a-zA-Z_0-9][a-zA-Z_0-9.-]*), so '=' is an invalid character for the table name.    * Region name is ([a-f0-9]+), so '-' is an invalid character for the region name.    * HFile is ([0-9a-f]+(?:_SeqId_[0-9]+_)?) covering the plain hfiles (uuid)    * and the bulk loaded (_SeqId_[0-9]+_) hfiles.    */
specifier|public
specifier|static
specifier|final
name|String
name|LINK_NAME_REGEX
init|=
name|String
operator|.
name|format
argument_list|(
literal|"%s=%s-%s"
argument_list|,
name|HTableDescriptor
operator|.
name|VALID_USER_TABLE_REGEX
argument_list|,
name|HRegionInfo
operator|.
name|ENCODED_REGION_NAME_REGEX
argument_list|,
name|StoreFile
operator|.
name|HFILE_NAME_REGEX
argument_list|)
decl_stmt|;
comment|/** Define the HFile Link name parser in the form of: table=region-hfile */
specifier|private
specifier|static
specifier|final
name|Pattern
name|LINK_NAME_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"^(%s)=(%s)-(%s)$"
argument_list|,
name|HTableDescriptor
operator|.
name|VALID_USER_TABLE_REGEX
argument_list|,
name|HRegionInfo
operator|.
name|ENCODED_REGION_NAME_REGEX
argument_list|,
name|StoreFile
operator|.
name|HFILE_NAME_REGEX
argument_list|)
argument_list|)
decl_stmt|;
comment|/**    * The link should be used for hfile and reference links    * that can be found in /hbase/table/region/family/    */
specifier|private
specifier|static
specifier|final
name|Pattern
name|REF_OR_HFILE_LINK_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"^(%s)=(%s)-(.+)$"
argument_list|,
name|HTableDescriptor
operator|.
name|VALID_USER_TABLE_REGEX
argument_list|,
name|HRegionInfo
operator|.
name|ENCODED_REGION_NAME_REGEX
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Path
name|archivePath
decl_stmt|;
specifier|private
specifier|final
name|Path
name|originPath
decl_stmt|;
comment|/**    * @param conf {@link Configuration} from which to extract specific archive locations    * @param path The path of the HFile Link.    * @throws IOException on unexpected error.    */
specifier|public
name|HFileLink
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|,
name|HFileArchiveUtil
operator|.
name|getArchivePath
argument_list|(
name|conf
argument_list|)
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param rootDir Path to the root directory where hbase files are stored    * @param archiveDir Path to the hbase archive directory    * @param path The path of the HFile Link.    */
specifier|public
name|HFileLink
parameter_list|(
specifier|final
name|Path
name|rootDir
parameter_list|,
specifier|final
name|Path
name|archiveDir
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|)
block|{
name|Path
name|hfilePath
init|=
name|getRelativeTablePath
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|this
operator|.
name|originPath
operator|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|hfilePath
argument_list|)
expr_stmt|;
name|this
operator|.
name|archivePath
operator|=
operator|new
name|Path
argument_list|(
name|archiveDir
argument_list|,
name|hfilePath
argument_list|)
expr_stmt|;
name|setLocations
argument_list|(
name|originPath
argument_list|,
name|archivePath
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param originPath Path to the hfile in the table directory    * @param archivePath Path to the hfile in the archive directory    */
specifier|public
name|HFileLink
parameter_list|(
specifier|final
name|Path
name|originPath
parameter_list|,
specifier|final
name|Path
name|archivePath
parameter_list|)
block|{
name|this
operator|.
name|originPath
operator|=
name|originPath
expr_stmt|;
name|this
operator|.
name|archivePath
operator|=
name|archivePath
expr_stmt|;
name|setLocations
argument_list|(
name|originPath
argument_list|,
name|archivePath
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return the origin path of the hfile.    */
specifier|public
name|Path
name|getOriginPath
parameter_list|()
block|{
return|return
name|this
operator|.
name|originPath
return|;
block|}
comment|/**    * @return the path of the archived hfile.    */
specifier|public
name|Path
name|getArchivePath
parameter_list|()
block|{
return|return
name|this
operator|.
name|archivePath
return|;
block|}
comment|/**    * @param path Path to check.    * @return True if the path is a HFileLink.    */
specifier|public
specifier|static
name|boolean
name|isHFileLink
parameter_list|(
specifier|final
name|Path
name|path
parameter_list|)
block|{
return|return
name|isHFileLink
argument_list|(
name|path
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param fileName File name to check.    * @return True if the path is a HFileLink.    */
specifier|public
specifier|static
name|boolean
name|isHFileLink
parameter_list|(
name|String
name|fileName
parameter_list|)
block|{
name|Matcher
name|m
init|=
name|LINK_NAME_PATTERN
operator|.
name|matcher
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|m
operator|.
name|matches
argument_list|()
condition|)
return|return
literal|false
return|;
return|return
name|m
operator|.
name|groupCount
argument_list|()
operator|>
literal|2
operator|&&
name|m
operator|.
name|group
argument_list|(
literal|3
argument_list|)
operator|!=
literal|null
operator|&&
name|m
operator|.
name|group
argument_list|(
literal|2
argument_list|)
operator|!=
literal|null
operator|&&
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|!=
literal|null
return|;
block|}
comment|/**    * The returned path can be the "original" file path like: /hbase/table/region/cf/hfile    * or a path to the archived file like: /hbase/.archive/table/region/cf/hfile    *    * @param fs {@link FileSystem} on which to check the HFileLink    * @param conf {@link Configuration} from which to extract specific archive locations    * @param path HFileLink path    * @return Referenced path (original path or archived path)    * @throws IOException on unexpected error.    */
specifier|public
specifier|static
name|Path
name|getReferencedPath
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getReferencedPath
argument_list|(
name|fs
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|,
name|HFileArchiveUtil
operator|.
name|getArchivePath
argument_list|(
name|conf
argument_list|)
argument_list|,
name|path
argument_list|)
return|;
block|}
comment|/**    * The returned path can be the "original" file path like: /hbase/table/region/cf/hfile    * or a path to the archived file like: /hbase/.archive/table/region/cf/hfile    *    * @param fs {@link FileSystem} on which to check the HFileLink    * @param rootDir root hbase directory    * @param archiveDir Path to the hbase archive directory    * @param path HFileLink path    * @return Referenced path (original path or archived path)    * @throws IOException on unexpected error.    */
specifier|public
specifier|static
name|Path
name|getReferencedPath
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|rootDir
parameter_list|,
specifier|final
name|Path
name|archiveDir
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|hfilePath
init|=
name|getRelativeTablePath
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|Path
name|originPath
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|hfilePath
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|originPath
argument_list|)
condition|)
block|{
return|return
name|originPath
return|;
block|}
return|return
operator|new
name|Path
argument_list|(
name|archiveDir
argument_list|,
name|hfilePath
argument_list|)
return|;
block|}
comment|/**    * Convert a HFileLink path to a table relative path.    * e.g. the link: /hbase/test/0123/cf/testtb=4567-abcd    *      becomes: /hbase/testtb/4567/cf/abcd    *    * @param path HFileLink path    * @return Relative table path    * @throws IOException on unexpected error.    */
specifier|private
specifier|static
name|Path
name|getRelativeTablePath
parameter_list|(
specifier|final
name|Path
name|path
parameter_list|)
block|{
comment|// table=region-hfile
name|Matcher
name|m
init|=
name|REF_OR_HFILE_LINK_PATTERN
operator|.
name|matcher
argument_list|(
name|path
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|path
operator|.
name|getName
argument_list|()
operator|+
literal|" is not a valid HFileLink name!"
argument_list|)
throw|;
block|}
comment|// Convert the HFileLink name into a real table/region/cf/hfile path.
name|String
name|tableName
init|=
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|String
name|regionName
init|=
name|m
operator|.
name|group
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|String
name|hfileName
init|=
name|m
operator|.
name|group
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|String
name|familyName
init|=
name|path
operator|.
name|getParent
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|tableName
argument_list|,
name|regionName
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|familyName
argument_list|,
name|hfileName
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Get the HFile name of the referenced link    *    * @param fileName HFileLink file name    * @return the name of the referenced HFile    */
specifier|public
specifier|static
name|String
name|getReferencedHFileName
parameter_list|(
specifier|final
name|String
name|fileName
parameter_list|)
block|{
name|Matcher
name|m
init|=
name|REF_OR_HFILE_LINK_PATTERN
operator|.
name|matcher
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|fileName
operator|+
literal|" is not a valid HFileLink name!"
argument_list|)
throw|;
block|}
return|return
operator|(
name|m
operator|.
name|group
argument_list|(
literal|3
argument_list|)
operator|)
return|;
block|}
comment|/**    * Get the Region name of the referenced link    *    * @param fileName HFileLink file name    * @return the name of the referenced Region    */
specifier|public
specifier|static
name|String
name|getReferencedRegionName
parameter_list|(
specifier|final
name|String
name|fileName
parameter_list|)
block|{
name|Matcher
name|m
init|=
name|REF_OR_HFILE_LINK_PATTERN
operator|.
name|matcher
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|fileName
operator|+
literal|" is not a valid HFileLink name!"
argument_list|)
throw|;
block|}
return|return
operator|(
name|m
operator|.
name|group
argument_list|(
literal|2
argument_list|)
operator|)
return|;
block|}
comment|/**    * Get the Table name of the referenced link    *    * @param fileName HFileLink file name    * @return the name of the referenced Table    */
specifier|public
specifier|static
name|String
name|getReferencedTableName
parameter_list|(
specifier|final
name|String
name|fileName
parameter_list|)
block|{
name|Matcher
name|m
init|=
name|REF_OR_HFILE_LINK_PATTERN
operator|.
name|matcher
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|fileName
operator|+
literal|" is not a valid HFileLink name!"
argument_list|)
throw|;
block|}
return|return
operator|(
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|)
return|;
block|}
comment|/**    * Create a new HFileLink name    *    * @param hfileRegionInfo - Linked HFile Region Info    * @param hfileName - Linked HFile name    * @return file name of the HFile Link    */
specifier|public
specifier|static
name|String
name|createHFileLinkName
parameter_list|(
specifier|final
name|HRegionInfo
name|hfileRegionInfo
parameter_list|,
specifier|final
name|String
name|hfileName
parameter_list|)
block|{
return|return
name|createHFileLinkName
argument_list|(
name|hfileRegionInfo
operator|.
name|getTableNameAsString
argument_list|()
argument_list|,
name|hfileRegionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|hfileName
argument_list|)
return|;
block|}
comment|/**    * Create a new HFileLink name    *    * @param tableName - Linked HFile table name    * @param regionName - Linked HFile region name    * @param hfileName - Linked HFile name    * @return file name of the HFile Link    */
specifier|public
specifier|static
name|String
name|createHFileLinkName
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|String
name|regionName
parameter_list|,
specifier|final
name|String
name|hfileName
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s=%s-%s"
argument_list|,
name|tableName
argument_list|,
name|regionName
argument_list|,
name|hfileName
argument_list|)
return|;
block|}
comment|/**    * Create a new HFileLink    *    *<p>It also adds a back-reference to the hfile back-reference directory    * to simplify the reference-count and the cleaning process.    *    * @param conf {@link Configuration} to read for the archive directory name    * @param fs {@link FileSystem} on which to write the HFileLink    * @param dstFamilyPath - Destination path (table/region/cf/)    * @param hfileRegionInfo - Linked HFile Region Info    * @param hfileName - Linked HFile name    * @return true if the file is created, otherwise the file exists.    * @throws IOException on file or parent directory creation failure    */
specifier|public
specifier|static
name|boolean
name|create
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|dstFamilyPath
parameter_list|,
specifier|final
name|HRegionInfo
name|hfileRegionInfo
parameter_list|,
specifier|final
name|String
name|hfileName
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|linkedTable
init|=
name|hfileRegionInfo
operator|.
name|getTableNameAsString
argument_list|()
decl_stmt|;
name|String
name|linkedRegion
init|=
name|hfileRegionInfo
operator|.
name|getEncodedName
argument_list|()
decl_stmt|;
return|return
name|create
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|dstFamilyPath
argument_list|,
name|linkedTable
argument_list|,
name|linkedRegion
argument_list|,
name|hfileName
argument_list|)
return|;
block|}
comment|/**    * Create a new HFileLink    *    *<p>It also adds a back-reference to the hfile back-reference directory    * to simplify the reference-count and the cleaning process.    *    * @param conf {@link Configuration} to read for the archive directory name    * @param fs {@link FileSystem} on which to write the HFileLink    * @param dstFamilyPath - Destination path (table/region/cf/)    * @param linkedTable - Linked Table Name    * @param linkedRegion - Linked Region Name    * @param hfileName - Linked HFile name    * @return true if the file is created, otherwise the file exists.    * @throws IOException on file or parent directory creation failure    */
specifier|public
specifier|static
name|boolean
name|create
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|dstFamilyPath
parameter_list|,
specifier|final
name|String
name|linkedTable
parameter_list|,
specifier|final
name|String
name|linkedRegion
parameter_list|,
specifier|final
name|String
name|hfileName
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|familyName
init|=
name|dstFamilyPath
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
name|regionName
init|=
name|dstFamilyPath
operator|.
name|getParent
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
name|tableName
init|=
name|dstFamilyPath
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
name|name
init|=
name|createHFileLinkName
argument_list|(
name|linkedTable
argument_list|,
name|linkedRegion
argument_list|,
name|hfileName
argument_list|)
decl_stmt|;
name|String
name|refName
init|=
name|createBackReferenceName
argument_list|(
name|tableName
argument_list|,
name|regionName
argument_list|)
decl_stmt|;
comment|// Make sure the destination directory exists
name|fs
operator|.
name|mkdirs
argument_list|(
name|dstFamilyPath
argument_list|)
expr_stmt|;
comment|// Make sure the FileLink reference directory exists
name|Path
name|archiveStoreDir
init|=
name|HFileArchiveUtil
operator|.
name|getStoreArchivePath
argument_list|(
name|conf
argument_list|,
name|linkedTable
argument_list|,
name|linkedRegion
argument_list|,
name|familyName
argument_list|)
decl_stmt|;
name|Path
name|backRefssDir
init|=
name|getBackReferencesDir
argument_list|(
name|archiveStoreDir
argument_list|,
name|hfileName
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|backRefssDir
argument_list|)
expr_stmt|;
comment|// Create the reference for the link
name|Path
name|backRefPath
init|=
operator|new
name|Path
argument_list|(
name|backRefssDir
argument_list|,
name|refName
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|backRefPath
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Create the link
return|return
name|fs
operator|.
name|createNewFile
argument_list|(
operator|new
name|Path
argument_list|(
name|dstFamilyPath
argument_list|,
name|name
argument_list|)
argument_list|)
return|;
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
literal|"couldn't create the link="
operator|+
name|name
operator|+
literal|" for "
operator|+
name|dstFamilyPath
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// Revert the reference if the link creation failed
name|fs
operator|.
name|delete
argument_list|(
name|backRefPath
argument_list|,
literal|false
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|/**    * Create a new HFileLink starting from a hfileLink name    *    *<p>It also adds a back-reference to the hfile back-reference directory    * to simplify the reference-count and the cleaning process.    *    * @param conf {@link Configuration} to read for the archive directory name    * @param fs {@link FileSystem} on which to write the HFileLink    * @param dstFamilyPath - Destination path (table/region/cf/)    * @param hfileLinkName - HFileLink name (it contains hfile-region-table)    * @return true if the file is created, otherwise the file exists.    * @throws IOException on file or parent directory creation failure    */
specifier|public
specifier|static
name|boolean
name|createFromHFileLink
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|dstFamilyPath
parameter_list|,
specifier|final
name|String
name|hfileLinkName
parameter_list|)
throws|throws
name|IOException
block|{
name|Matcher
name|m
init|=
name|LINK_NAME_PATTERN
operator|.
name|matcher
argument_list|(
name|hfileLinkName
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|hfileLinkName
operator|+
literal|" is not a valid HFileLink name!"
argument_list|)
throw|;
block|}
return|return
name|create
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|dstFamilyPath
argument_list|,
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|,
name|m
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|,
name|m
operator|.
name|group
argument_list|(
literal|3
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Create the back reference name    */
specifier|private
specifier|static
name|String
name|createBackReferenceName
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|String
name|regionName
parameter_list|)
block|{
return|return
name|regionName
operator|+
literal|"."
operator|+
name|tableName
return|;
block|}
comment|/**    * Get the full path of the HFile referenced by the back reference    *    * @param rootDir root hbase directory    * @param linkRefPath Link Back Reference path    * @return full path of the referenced hfile    * @throws IOException on unexpected error.    */
specifier|public
specifier|static
name|Path
name|getHFileFromBackReference
parameter_list|(
specifier|final
name|Path
name|rootDir
parameter_list|,
specifier|final
name|Path
name|linkRefPath
parameter_list|)
block|{
name|int
name|separatorIndex
init|=
name|linkRefPath
operator|.
name|getName
argument_list|()
operator|.
name|indexOf
argument_list|(
literal|'.'
argument_list|)
decl_stmt|;
name|String
name|linkRegionName
init|=
name|linkRefPath
operator|.
name|getName
argument_list|()
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|separatorIndex
argument_list|)
decl_stmt|;
name|String
name|linkTableName
init|=
name|linkRefPath
operator|.
name|getName
argument_list|()
operator|.
name|substring
argument_list|(
name|separatorIndex
operator|+
literal|1
argument_list|)
decl_stmt|;
name|String
name|hfileName
init|=
name|getBackReferenceFileName
argument_list|(
name|linkRefPath
operator|.
name|getParent
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|familyPath
init|=
name|linkRefPath
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|Path
name|regionPath
init|=
name|familyPath
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|Path
name|tablePath
init|=
name|regionPath
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|String
name|linkName
init|=
name|createHFileLinkName
argument_list|(
name|tablePath
operator|.
name|getName
argument_list|()
argument_list|,
name|regionPath
operator|.
name|getName
argument_list|()
argument_list|,
name|hfileName
argument_list|)
decl_stmt|;
name|Path
name|linkTableDir
init|=
name|FSUtils
operator|.
name|getTablePath
argument_list|(
name|rootDir
argument_list|,
name|linkTableName
argument_list|)
decl_stmt|;
name|Path
name|regionDir
init|=
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|linkTableDir
argument_list|,
name|linkRegionName
argument_list|)
decl_stmt|;
return|return
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|regionDir
argument_list|,
name|familyPath
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|linkName
argument_list|)
return|;
block|}
comment|/**    * Get the full path of the HFile referenced by the back reference    *    * @param conf {@link Configuration} to read for the archive directory name    * @param linkRefPath Link Back Reference path    * @return full path of the referenced hfile    * @throws IOException on unexpected error.    */
specifier|public
specifier|static
name|Path
name|getHFileFromBackReference
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Path
name|linkRefPath
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getHFileFromBackReference
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|,
name|linkRefPath
argument_list|)
return|;
block|}
block|}
end_class

end_unit

