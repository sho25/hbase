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
name|catalog
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
name|HashSet
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
name|catalog
operator|.
name|MetaReader
operator|.
name|Visitor
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
name|Put
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
name|Result
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
name|MasterServices
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
name|migration
operator|.
name|HRegionInfo090x
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
name|Writables
import|;
end_import

begin_comment
comment|/**  * Tools to help with migration of meta tables so they no longer host  * instances of HTableDescriptor.  * @deprecated Used migration from 0.90 to 0.92 so will be going away in next  * release  */
end_comment

begin_class
specifier|public
class|class
name|MetaMigrationRemovingHTD
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
name|MetaMigrationRemovingHTD
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Update legacy META rows, removing HTD from HRI.    * @param masterServices    * @return List of table descriptors.    * @throws IOException    */
specifier|public
specifier|static
name|Set
argument_list|<
name|HTableDescriptor
argument_list|>
name|updateMetaWithNewRegionInfo
parameter_list|(
specifier|final
name|MasterServices
name|masterServices
parameter_list|)
throws|throws
name|IOException
block|{
name|MigratingVisitor
name|v
init|=
operator|new
name|MigratingVisitor
argument_list|(
name|masterServices
argument_list|)
decl_stmt|;
name|MetaReader
operator|.
name|fullScan
argument_list|(
name|masterServices
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|v
argument_list|)
expr_stmt|;
name|updateRootWithMetaMigrationStatus
argument_list|(
name|masterServices
operator|.
name|getCatalogTracker
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|v
operator|.
name|htds
return|;
block|}
comment|/**    * Update the ROOT with new HRI. (HRI with no HTD)    * @param masterServices    * @return List of table descriptors    * @throws IOException    */
specifier|static
name|Set
argument_list|<
name|HTableDescriptor
argument_list|>
name|updateRootWithNewRegionInfo
parameter_list|(
specifier|final
name|MasterServices
name|masterServices
parameter_list|)
throws|throws
name|IOException
block|{
name|MigratingVisitor
name|v
init|=
operator|new
name|MigratingVisitor
argument_list|(
name|masterServices
argument_list|)
decl_stmt|;
name|MetaReader
operator|.
name|fullScan
argument_list|(
name|masterServices
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|v
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
name|v
operator|.
name|htds
return|;
block|}
comment|/**    * Meta visitor that migrates the info:regioninfo as it visits.    */
specifier|static
class|class
name|MigratingVisitor
implements|implements
name|Visitor
block|{
specifier|private
specifier|final
name|MasterServices
name|services
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|HTableDescriptor
argument_list|>
name|htds
init|=
operator|new
name|HashSet
argument_list|<
name|HTableDescriptor
argument_list|>
argument_list|()
decl_stmt|;
name|MigratingVisitor
parameter_list|(
specifier|final
name|MasterServices
name|services
parameter_list|)
block|{
name|this
operator|.
name|services
operator|=
name|services
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|visit
parameter_list|(
name|Result
name|r
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|r
operator|==
literal|null
operator|||
name|r
operator|.
name|isEmpty
argument_list|()
condition|)
return|return
literal|true
return|;
comment|// Check info:regioninfo, info:splitA, and info:splitB.  Make sure all
comment|// have migrated HRegionInfos... that there are no leftover 090 version
comment|// HRegionInfos.
name|byte
index|[]
name|hriBytes
init|=
name|getBytes
argument_list|(
name|r
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
decl_stmt|;
comment|// Presumes that an edit updating all three cells either succeeds or
comment|// doesn't -- that we don't have case of info:regioninfo migrated but not
comment|// info:splitA.
if|if
condition|(
name|isMigrated
argument_list|(
name|hriBytes
argument_list|)
condition|)
return|return
literal|true
return|;
comment|// OK. Need to migrate this row in meta.
name|HRegionInfo090x
name|hri090
init|=
name|getHRegionInfo090x
argument_list|(
name|hriBytes
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
name|hri090
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|htd
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"A 090 HRI has null HTD? Continuing; "
operator|+
name|hri090
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
name|this
operator|.
name|htds
operator|.
name|contains
argument_list|(
name|htd
argument_list|)
condition|)
block|{
comment|// If first time we are adding a table, then write it out to fs.
comment|// Presumes that first region in table has THE table's schema which
comment|// might not be too bad of a presumption since it'll be first region
comment|// 'altered'
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|createTableDescriptor
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|this
operator|.
name|htds
operator|.
name|add
argument_list|(
name|htd
argument_list|)
expr_stmt|;
block|}
comment|// This will 'migrate' the hregioninfo from 090 version to 092.
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|hri090
argument_list|)
decl_stmt|;
comment|// Now make a put to write back to meta.
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|hri
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now check info:splitA and info:splitB if present.  Migrate these too.
name|checkSplit
argument_list|(
name|r
argument_list|,
name|p
argument_list|,
name|HConstants
operator|.
name|SPLITA_QUALIFIER
argument_list|)
expr_stmt|;
name|checkSplit
argument_list|(
name|r
argument_list|,
name|p
argument_list|,
name|HConstants
operator|.
name|SPLITB_QUALIFIER
argument_list|)
expr_stmt|;
comment|// Below we fake out putToCatalogTable
name|MetaEditor
operator|.
name|putToCatalogTable
argument_list|(
name|this
operator|.
name|services
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|p
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Migrated "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|p
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
specifier|static
name|void
name|checkSplit
parameter_list|(
specifier|final
name|Result
name|r
parameter_list|,
specifier|final
name|Put
name|p
parameter_list|,
specifier|final
name|byte
index|[]
name|which
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|hriSplitBytes
init|=
name|getBytes
argument_list|(
name|r
argument_list|,
name|which
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|isMigrated
argument_list|(
name|hriSplitBytes
argument_list|)
condition|)
block|{
comment|// This will convert the HRI from 090 to 092 HRI.
name|HRegionInfo
name|hri
init|=
name|Writables
operator|.
name|getHRegionInfo
argument_list|(
name|hriSplitBytes
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|which
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|hri
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @param r Result to dig in.    * @param qualifier Qualifier to look at in the passed<code>r</code>.    * @return Bytes for an HRegionInfo or null if no bytes or empty bytes found.    */
specifier|static
name|byte
index|[]
name|getBytes
parameter_list|(
specifier|final
name|Result
name|r
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
name|byte
index|[]
name|hriBytes
init|=
name|r
operator|.
name|getValue
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
if|if
condition|(
name|hriBytes
operator|==
literal|null
operator|||
name|hriBytes
operator|.
name|length
operator|<=
literal|0
condition|)
return|return
literal|null
return|;
return|return
name|hriBytes
return|;
block|}
comment|/**    * @param r Result to look in.    * @param qualifier What to look at in the passed result.    * @return Either a 090 vintage HRegionInfo OR null if no HRegionInfo or    * the HRegionInfo is up to date and not in need of migration.    * @throws IOException    */
specifier|static
name|HRegionInfo090x
name|get090HRI
parameter_list|(
specifier|final
name|Result
name|r
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|hriBytes
init|=
name|r
operator|.
name|getValue
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
if|if
condition|(
name|hriBytes
operator|==
literal|null
operator|||
name|hriBytes
operator|.
name|length
operator|<=
literal|0
condition|)
return|return
literal|null
return|;
if|if
condition|(
name|isMigrated
argument_list|(
name|hriBytes
argument_list|)
condition|)
return|return
literal|null
return|;
return|return
name|getHRegionInfo090x
argument_list|(
name|hriBytes
argument_list|)
return|;
block|}
specifier|static
name|boolean
name|isMigrated
parameter_list|(
specifier|final
name|byte
index|[]
name|hriBytes
parameter_list|)
block|{
if|if
condition|(
name|hriBytes
operator|==
literal|null
operator|||
name|hriBytes
operator|.
name|length
operator|<=
literal|0
condition|)
return|return
literal|true
return|;
comment|// Else, what version this HRegionInfo instance is at.  The first byte
comment|// is the version byte in a serialized HRegionInfo.  If its same as our
comment|// current HRI, then nothing to do.
if|if
condition|(
name|hriBytes
index|[
literal|0
index|]
operator|==
name|HRegionInfo
operator|.
name|VERSION
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|hriBytes
index|[
literal|0
index|]
operator|==
name|HRegionInfo
operator|.
name|VERSION_PRE_092
condition|)
return|return
literal|false
return|;
comment|// Unknown version.  Return true that its 'migrated' but log warning.
comment|// Should 'never' happen.
assert|assert
literal|false
operator|:
literal|"Unexpected version; bytes="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|hriBytes
argument_list|)
assert|;
return|return
literal|true
return|;
block|}
comment|/**    * Migrate root and meta to newer version. This updates the META and ROOT    * and removes the HTD from HRI.    * @param masterServices    * @throws IOException    */
specifier|public
specifier|static
name|void
name|migrateRootAndMeta
parameter_list|(
specifier|final
name|MasterServices
name|masterServices
parameter_list|)
throws|throws
name|IOException
block|{
name|updateRootWithNewRegionInfo
argument_list|(
name|masterServices
argument_list|)
expr_stmt|;
name|updateMetaWithNewRegionInfo
argument_list|(
name|masterServices
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update the version flag in -ROOT-.    * @param catalogTracker    * @throws IOException    */
specifier|public
specifier|static
name|void
name|updateRootWithMetaMigrationStatus
parameter_list|(
specifier|final
name|CatalogTracker
name|catalogTracker
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|MetaEditor
operator|.
name|putToRootTable
argument_list|(
name|catalogTracker
argument_list|,
name|setMetaVersion
argument_list|(
name|p
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Updated -ROOT- meta version="
operator|+
name|HConstants
operator|.
name|META_VERSION
argument_list|)
expr_stmt|;
block|}
specifier|static
name|Put
name|setMetaVersion
parameter_list|(
specifier|final
name|Put
name|p
parameter_list|)
block|{
name|p
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|META_VERSION_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|HConstants
operator|.
name|META_VERSION
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|p
return|;
block|}
comment|/**    * @return True if the meta table has been migrated.    * @throws IOException    */
comment|// Public because used in tests
specifier|public
specifier|static
name|boolean
name|isMetaHRIUpdated
parameter_list|(
specifier|final
name|MasterServices
name|services
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|MetaReader
operator|.
name|fullScanOfRoot
argument_list|(
name|services
operator|.
name|getCatalogTracker
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|results
operator|==
literal|null
operator|||
name|results
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Not migrated"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// Presume only the one result because we only support on meta region.
name|Result
name|r
init|=
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|short
name|version
init|=
name|getMetaVersion
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|boolean
name|migrated
init|=
name|version
operator|>=
name|HConstants
operator|.
name|META_VERSION
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Meta version="
operator|+
name|version
operator|+
literal|"; migrated="
operator|+
name|migrated
argument_list|)
expr_stmt|;
return|return
name|migrated
return|;
block|}
comment|/**    * @param r Result to look at    * @return Current meta table version or -1 if no version found.    */
specifier|static
name|short
name|getMetaVersion
parameter_list|(
specifier|final
name|Result
name|r
parameter_list|)
block|{
name|byte
index|[]
name|value
init|=
name|r
operator|.
name|getValue
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|META_VERSION_QUALIFIER
argument_list|)
decl_stmt|;
return|return
name|value
operator|==
literal|null
operator|||
name|value
operator|.
name|length
operator|<=
literal|0
condition|?
operator|-
literal|1
else|:
name|Bytes
operator|.
name|toShort
argument_list|(
name|value
argument_list|)
return|;
block|}
comment|/**    * @return True if migrated.    * @throws IOException    */
specifier|public
specifier|static
name|boolean
name|updateMetaWithNewHRI
parameter_list|(
specifier|final
name|MasterServices
name|services
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|isMetaHRIUpdated
argument_list|(
name|services
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"ROOT/Meta already up-to date with new HRI."
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Meta has HRI with HTDs. Updating meta now."
argument_list|)
expr_stmt|;
try|try
block|{
name|migrateRootAndMeta
argument_list|(
name|services
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"ROOT and Meta updated with new HRI."
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Update ROOT/Meta with new HRI failed."
operator|+
literal|"Master startup aborted."
argument_list|)
throw|;
block|}
block|}
comment|/**    * Get HREgionInfoForMigration serialized from bytes.    * @param bytes serialized bytes    * @return An instance of a 090 HRI or null if we failed deserialize    */
specifier|public
specifier|static
name|HRegionInfo090x
name|getHRegionInfo090x
parameter_list|(
specifier|final
name|byte
index|[]
name|bytes
parameter_list|)
block|{
if|if
condition|(
name|bytes
operator|==
literal|null
operator|||
name|bytes
operator|.
name|length
operator|==
literal|0
condition|)
return|return
literal|null
return|;
name|HRegionInfo090x
name|hri
init|=
literal|null
decl_stmt|;
try|try
block|{
name|hri
operator|=
operator|(
name|HRegionInfo090x
operator|)
name|Writables
operator|.
name|getWritable
argument_list|(
name|bytes
argument_list|,
operator|new
name|HRegionInfo090x
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed deserialize as a 090 HRegionInfo); bytes="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|bytes
argument_list|)
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
return|return
name|hri
return|;
block|}
block|}
end_class

end_unit

