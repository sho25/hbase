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
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|ConnectException
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
name|List
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
name|DoNotRetryIOException
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
name|NotAllMetaRegionsOnlineException
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
name|client
operator|.
name|Delete
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
name|Get
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
name|HTable
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
name|Mutation
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
name|ipc
operator|.
name|CoprocessorRpcChannel
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
name|ClientProtos
operator|.
name|MutationProto
operator|.
name|MutationType
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
name|MultiRowMutation
operator|.
name|MultiMutateRequest
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
name|MultiRowMutation
operator|.
name|MultiRowMutationService
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
comment|/**  * Writes region and assignment information to<code>.META.</code>.  * TODO: Put MetaReader and MetaEditor together; doesn't make sense having  * them distinct. see HBASE-3475.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetaEditor
block|{
comment|// TODO: Strip CatalogTracker from this class.  Its all over and in the end
comment|// its only used to get its Configuration so we can get associated
comment|// Connection.
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
name|MetaEditor
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Generates and returns a Put containing the region into for the catalog table    */
specifier|public
specifier|static
name|Put
name|makePutFromRegionInfo
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|addRegionInfo
argument_list|(
name|put
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
return|return
name|put
return|;
block|}
comment|/**    * Generates and returns a Delete containing the region info for the catalog    * table    */
specifier|public
specifier|static
name|Delete
name|makeDeleteFromRegionInfo
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
if|if
condition|(
name|regionInfo
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't make a delete for null region"
argument_list|)
throw|;
block|}
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|delete
return|;
block|}
comment|/**    * Adds split daughters to the Put    */
specifier|public
specifier|static
name|Put
name|addDaughtersToPut
parameter_list|(
name|Put
name|put
parameter_list|,
name|HRegionInfo
name|splitA
parameter_list|,
name|HRegionInfo
name|splitB
parameter_list|)
block|{
if|if
condition|(
name|splitA
operator|!=
literal|null
condition|)
block|{
name|put
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|SPLITA_QUALIFIER
argument_list|,
name|splitA
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|splitB
operator|!=
literal|null
condition|)
block|{
name|put
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|SPLITB_QUALIFIER
argument_list|,
name|splitB
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|put
return|;
block|}
comment|/**    * Put the passed<code>p</code> to the<code>.META.</code> table.    * @param ct CatalogTracker on whose back we will ride the edit.    * @param p Put to add to .META.    * @throws IOException    */
specifier|static
name|void
name|putToMetaTable
parameter_list|(
specifier|final
name|CatalogTracker
name|ct
parameter_list|,
specifier|final
name|Put
name|p
parameter_list|)
throws|throws
name|IOException
block|{
name|put
argument_list|(
name|MetaReader
operator|.
name|getMetaHTable
argument_list|(
name|ct
argument_list|)
argument_list|,
name|p
argument_list|)
expr_stmt|;
block|}
comment|/**    * Put the passed<code>p</code> to a catalog table.    * @param ct CatalogTracker on whose back we will ride the edit.    * @param p Put to add    * @throws IOException    */
specifier|static
name|void
name|putToCatalogTable
parameter_list|(
specifier|final
name|CatalogTracker
name|ct
parameter_list|,
specifier|final
name|Put
name|p
parameter_list|)
throws|throws
name|IOException
block|{
name|put
argument_list|(
name|MetaReader
operator|.
name|getCatalogHTable
argument_list|(
name|ct
argument_list|)
argument_list|,
name|p
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param t Table to use (will be closed when done).    * @param p    * @throws IOException    */
specifier|private
specifier|static
name|void
name|put
parameter_list|(
specifier|final
name|HTable
name|t
parameter_list|,
specifier|final
name|Put
name|p
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Put the passed<code>ps</code> to the<code>.META.</code> table.    * @param ct CatalogTracker on whose back we will ride the edit.    * @param ps Put to add to .META.    * @throws IOException    */
specifier|public
specifier|static
name|void
name|putsToMetaTable
parameter_list|(
specifier|final
name|CatalogTracker
name|ct
parameter_list|,
specifier|final
name|List
argument_list|<
name|Put
argument_list|>
name|ps
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|t
init|=
name|MetaReader
operator|.
name|getMetaHTable
argument_list|(
name|ct
argument_list|)
decl_stmt|;
try|try
block|{
name|t
operator|.
name|put
argument_list|(
name|ps
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Delete the passed<code>d</code> from the<code>.META.</code> table.    * @param ct CatalogTracker on whose back we will ride the edit.    * @param d Delete to add to .META.    * @throws IOException    */
specifier|static
name|void
name|deleteFromMetaTable
parameter_list|(
specifier|final
name|CatalogTracker
name|ct
parameter_list|,
specifier|final
name|Delete
name|d
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Delete
argument_list|>
name|dels
init|=
operator|new
name|ArrayList
argument_list|<
name|Delete
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|dels
operator|.
name|add
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|deleteFromMetaTable
argument_list|(
name|ct
argument_list|,
name|dels
argument_list|)
expr_stmt|;
block|}
comment|/**    * Delete the passed<code>deletes</code> from the<code>.META.</code> table.    * @param ct CatalogTracker on whose back we will ride the edit.    * @param deletes Deletes to add to .META.  This list should support #remove.    * @throws IOException    */
specifier|public
specifier|static
name|void
name|deleteFromMetaTable
parameter_list|(
specifier|final
name|CatalogTracker
name|ct
parameter_list|,
specifier|final
name|List
argument_list|<
name|Delete
argument_list|>
name|deletes
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|t
init|=
name|MetaReader
operator|.
name|getMetaHTable
argument_list|(
name|ct
argument_list|)
decl_stmt|;
try|try
block|{
name|t
operator|.
name|delete
argument_list|(
name|deletes
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Execute the passed<code>mutations</code> against<code>.META.</code> table.    * @param ct CatalogTracker on whose back we will ride the edit.    * @param mutations Puts and Deletes to execute on .META.    * @throws IOException    */
specifier|static
name|void
name|mutateMetaTable
parameter_list|(
specifier|final
name|CatalogTracker
name|ct
parameter_list|,
specifier|final
name|List
argument_list|<
name|Mutation
argument_list|>
name|mutations
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|t
init|=
name|MetaReader
operator|.
name|getMetaHTable
argument_list|(
name|ct
argument_list|)
decl_stmt|;
try|try
block|{
name|t
operator|.
name|batch
argument_list|(
name|mutations
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|InterruptedIOException
name|ie
init|=
operator|new
name|InterruptedIOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|ie
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|ie
throw|;
block|}
finally|finally
block|{
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Adds a META row for the specified new region.    * @param regionInfo region information    * @throws IOException if problem connecting or updating meta    */
specifier|public
specifier|static
name|void
name|addRegionToMeta
parameter_list|(
name|CatalogTracker
name|catalogTracker
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|putToMetaTable
argument_list|(
name|catalogTracker
argument_list|,
name|makePutFromRegionInfo
argument_list|(
name|regionInfo
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Added "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Adds a META row for the specified new region to the given catalog table. The    * HTable is not flushed or closed.    * @param meta the HTable for META    * @param regionInfo region information    * @throws IOException if problem connecting or updating meta    */
specifier|public
specifier|static
name|void
name|addRegionToMeta
parameter_list|(
name|HTable
name|meta
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|addRegionToMeta
argument_list|(
name|meta
argument_list|,
name|regionInfo
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Adds a (single) META row for the specified new region and its daughters. Note that this does    * not add its daughter's as different rows, but adds information about the daughters    * in the same row as the parent. Use    * {@link #splitRegion(CatalogTracker, HRegionInfo, HRegionInfo, HRegionInfo, ServerName)}    * if you want to do that.    * @param meta the HTable for META    * @param regionInfo region information    * @param splitA first split daughter of the parent regionInfo    * @param splitB second split daughter of the parent regionInfo    * @throws IOException if problem connecting or updating meta    */
specifier|public
specifier|static
name|void
name|addRegionToMeta
parameter_list|(
name|HTable
name|meta
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|HRegionInfo
name|splitA
parameter_list|,
name|HRegionInfo
name|splitB
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|put
init|=
name|makePutFromRegionInfo
argument_list|(
name|regionInfo
argument_list|)
decl_stmt|;
name|addDaughtersToPut
argument_list|(
name|put
argument_list|,
name|splitA
argument_list|,
name|splitB
argument_list|)
expr_stmt|;
name|meta
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Added "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Adds a (single) META row for the specified new region and its daughters. Note that this does    * not add its daughter's as different rows, but adds information about the daughters    * in the same row as the parent. Use    * {@link #splitRegion(CatalogTracker, HRegionInfo, HRegionInfo, HRegionInfo, ServerName)}    * if you want to do that.    * @param catalogTracker CatalogTracker on whose back we will ride the edit.    * @param regionInfo region information    * @param splitA first split daughter of the parent regionInfo    * @param splitB second split daughter of the parent regionInfo    * @throws IOException if problem connecting or updating meta    */
specifier|public
specifier|static
name|void
name|addRegionToMeta
parameter_list|(
name|CatalogTracker
name|catalogTracker
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|HRegionInfo
name|splitA
parameter_list|,
name|HRegionInfo
name|splitB
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|meta
init|=
name|MetaReader
operator|.
name|getMetaHTable
argument_list|(
name|catalogTracker
argument_list|)
decl_stmt|;
try|try
block|{
name|addRegionToMeta
argument_list|(
name|meta
argument_list|,
name|regionInfo
argument_list|,
name|splitA
argument_list|,
name|splitB
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|meta
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Adds a META row for each of the specified new regions.    * @param catalogTracker CatalogTracker    * @param regionInfos region information list    * @throws IOException if problem connecting or updating meta    */
specifier|public
specifier|static
name|void
name|addRegionsToMeta
parameter_list|(
name|CatalogTracker
name|catalogTracker
parameter_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfos
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<
name|Put
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|regionInfo
range|:
name|regionInfos
control|)
block|{
name|puts
operator|.
name|add
argument_list|(
name|makePutFromRegionInfo
argument_list|(
name|regionInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|putsToMetaTable
argument_list|(
name|catalogTracker
argument_list|,
name|puts
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Added "
operator|+
name|puts
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Adds a daughter region entry to meta.    * @param regionInfo the region to put    * @param sn the location of the region    * @param openSeqNum the latest sequence number obtained when the region was open    */
specifier|public
specifier|static
name|void
name|addDaughter
parameter_list|(
specifier|final
name|CatalogTracker
name|catalogTracker
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|ServerName
name|sn
parameter_list|,
specifier|final
name|long
name|openSeqNum
parameter_list|)
throws|throws
name|NotAllMetaRegionsOnlineException
throws|,
name|IOException
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|addRegionInfo
argument_list|(
name|put
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
if|if
condition|(
name|sn
operator|!=
literal|null
condition|)
block|{
name|addLocation
argument_list|(
name|put
argument_list|,
name|sn
argument_list|,
name|openSeqNum
argument_list|)
expr_stmt|;
block|}
name|putToMetaTable
argument_list|(
name|catalogTracker
argument_list|,
name|put
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Added daughter "
operator|+
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
operator|+
operator|(
name|sn
operator|==
literal|null
condition|?
literal|", serverName=null"
else|:
literal|", serverName="
operator|+
name|sn
operator|.
name|toString
argument_list|()
operator|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Merge the two regions into one in an atomic operation. Deletes the two    * merging regions in META and adds the merged region with the information of    * two merging regions.    * @param catalogTracker the catalog tracker    * @param mergedRegion the merged region    * @param regionA    * @param regionB    * @param sn the location of the region    * @throws IOException    */
specifier|public
specifier|static
name|void
name|mergeRegions
parameter_list|(
specifier|final
name|CatalogTracker
name|catalogTracker
parameter_list|,
name|HRegionInfo
name|mergedRegion
parameter_list|,
name|HRegionInfo
name|regionA
parameter_list|,
name|HRegionInfo
name|regionB
parameter_list|,
name|ServerName
name|sn
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|meta
init|=
name|MetaReader
operator|.
name|getMetaHTable
argument_list|(
name|catalogTracker
argument_list|)
decl_stmt|;
try|try
block|{
name|HRegionInfo
name|copyOfMerged
init|=
operator|new
name|HRegionInfo
argument_list|(
name|mergedRegion
argument_list|)
decl_stmt|;
comment|// Put for parent
name|Put
name|putOfMerged
init|=
name|makePutFromRegionInfo
argument_list|(
name|copyOfMerged
argument_list|)
decl_stmt|;
name|putOfMerged
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|MERGEA_QUALIFIER
argument_list|,
name|regionA
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|putOfMerged
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|MERGEB_QUALIFIER
argument_list|,
name|regionB
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
comment|// Deletes for merging regions
name|Delete
name|deleteA
init|=
name|makeDeleteFromRegionInfo
argument_list|(
name|regionA
argument_list|)
decl_stmt|;
name|Delete
name|deleteB
init|=
name|makeDeleteFromRegionInfo
argument_list|(
name|regionB
argument_list|)
decl_stmt|;
comment|// The merged is a new region, openSeqNum = 1 is fine.
name|addLocation
argument_list|(
name|putOfMerged
argument_list|,
name|sn
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|byte
index|[]
name|tableRow
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|mergedRegion
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
name|HConstants
operator|.
name|DELIMITER
argument_list|)
decl_stmt|;
name|multiMutate
argument_list|(
name|meta
argument_list|,
name|tableRow
argument_list|,
name|putOfMerged
argument_list|,
name|deleteA
argument_list|,
name|deleteB
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|meta
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Splits the region into two in an atomic operation. Offlines the parent    * region with the information that it is split into two, and also adds    * the daughter regions. Does not add the location information to the daughter    * regions since they are not open yet.    * @param catalogTracker the catalog tracker    * @param parent the parent region which is split    * @param splitA Split daughter region A    * @param splitB Split daughter region A    * @param sn the location of the region    */
specifier|public
specifier|static
name|void
name|splitRegion
parameter_list|(
specifier|final
name|CatalogTracker
name|catalogTracker
parameter_list|,
name|HRegionInfo
name|parent
parameter_list|,
name|HRegionInfo
name|splitA
parameter_list|,
name|HRegionInfo
name|splitB
parameter_list|,
name|ServerName
name|sn
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|meta
init|=
name|MetaReader
operator|.
name|getMetaHTable
argument_list|(
name|catalogTracker
argument_list|)
decl_stmt|;
try|try
block|{
name|HRegionInfo
name|copyOfParent
init|=
operator|new
name|HRegionInfo
argument_list|(
name|parent
argument_list|)
decl_stmt|;
name|copyOfParent
operator|.
name|setOffline
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|copyOfParent
operator|.
name|setSplit
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|//Put for parent
name|Put
name|putParent
init|=
name|makePutFromRegionInfo
argument_list|(
name|copyOfParent
argument_list|)
decl_stmt|;
name|addDaughtersToPut
argument_list|(
name|putParent
argument_list|,
name|splitA
argument_list|,
name|splitB
argument_list|)
expr_stmt|;
comment|//Puts for daughters
name|Put
name|putA
init|=
name|makePutFromRegionInfo
argument_list|(
name|splitA
argument_list|)
decl_stmt|;
name|Put
name|putB
init|=
name|makePutFromRegionInfo
argument_list|(
name|splitB
argument_list|)
decl_stmt|;
name|addLocation
argument_list|(
name|putA
argument_list|,
name|sn
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|//these are new regions, openSeqNum = 1 is fine.
name|addLocation
argument_list|(
name|putB
argument_list|,
name|sn
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|byte
index|[]
name|tableRow
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|parent
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
name|HConstants
operator|.
name|DELIMITER
argument_list|)
decl_stmt|;
name|multiMutate
argument_list|(
name|meta
argument_list|,
name|tableRow
argument_list|,
name|putParent
argument_list|,
name|putA
argument_list|,
name|putB
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|meta
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Performs an atomic multi-Mutate operation against the given table.    */
specifier|private
specifier|static
name|void
name|multiMutate
parameter_list|(
name|HTable
name|table
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|Mutation
modifier|...
name|mutations
parameter_list|)
throws|throws
name|IOException
block|{
name|CoprocessorRpcChannel
name|channel
init|=
name|table
operator|.
name|coprocessorService
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|MultiMutateRequest
operator|.
name|Builder
name|mmrBuilder
init|=
name|MultiMutateRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Mutation
name|mutation
range|:
name|mutations
control|)
block|{
if|if
condition|(
name|mutation
operator|instanceof
name|Put
condition|)
block|{
name|mmrBuilder
operator|.
name|addMutationRequest
argument_list|(
name|ProtobufUtil
operator|.
name|toMutation
argument_list|(
name|MutationType
operator|.
name|PUT
argument_list|,
name|mutation
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|mutation
operator|instanceof
name|Delete
condition|)
block|{
name|mmrBuilder
operator|.
name|addMutationRequest
argument_list|(
name|ProtobufUtil
operator|.
name|toMutation
argument_list|(
name|MutationType
operator|.
name|DELETE
argument_list|,
name|mutation
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"multi in MetaEditor doesn't support "
operator|+
name|mutation
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|MultiRowMutationService
operator|.
name|BlockingInterface
name|service
init|=
name|MultiRowMutationService
operator|.
name|newBlockingStub
argument_list|(
name|channel
argument_list|)
decl_stmt|;
try|try
block|{
name|service
operator|.
name|mutateRows
argument_list|(
literal|null
argument_list|,
name|mmrBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|ex
parameter_list|)
block|{
name|ProtobufUtil
operator|.
name|toIOException
argument_list|(
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Updates the location of the specified META region in ROOT to be the    * specified server hostname and startcode.    *<p>    * Uses passed catalog tracker to get a connection to the server hosting    * ROOT and makes edits to that region.    *    * @param catalogTracker catalog tracker    * @param regionInfo region to update location of    * @param sn Server name    * @param openSeqNum the latest sequence number obtained when the region was open    * @throws IOException    * @throws ConnectException Usually because the regionserver carrying .META.    * is down.    * @throws NullPointerException Because no -ROOT- server connection    */
specifier|public
specifier|static
name|void
name|updateMetaLocation
parameter_list|(
name|CatalogTracker
name|catalogTracker
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|ServerName
name|sn
parameter_list|,
name|long
name|openSeqNum
parameter_list|)
throws|throws
name|IOException
throws|,
name|ConnectException
block|{
name|updateLocation
argument_list|(
name|catalogTracker
argument_list|,
name|regionInfo
argument_list|,
name|sn
argument_list|,
name|openSeqNum
argument_list|)
expr_stmt|;
block|}
comment|/**    * Updates the location of the specified region in META to be the specified    * server hostname and startcode.    *<p>    * Uses passed catalog tracker to get a connection to the server hosting    * META and makes edits to that region.    *    * @param catalogTracker catalog tracker    * @param regionInfo region to update location of    * @param sn Server name    * @throws IOException    */
specifier|public
specifier|static
name|void
name|updateRegionLocation
parameter_list|(
name|CatalogTracker
name|catalogTracker
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|ServerName
name|sn
parameter_list|,
name|long
name|updateSeqNum
parameter_list|)
throws|throws
name|IOException
block|{
name|updateLocation
argument_list|(
name|catalogTracker
argument_list|,
name|regionInfo
argument_list|,
name|sn
argument_list|,
name|updateSeqNum
argument_list|)
expr_stmt|;
block|}
comment|/**    * Updates the location of the specified region to be the specified server.    *<p>    * Connects to the specified server which should be hosting the specified    * catalog region name to perform the edit.    *    * @param catalogTracker    * @param regionInfo region to update location of    * @param sn Server name    * @param openSeqNum the latest sequence number obtained when the region was open    * @throws IOException In particular could throw {@link java.net.ConnectException}    * if the server is down on other end.    */
specifier|private
specifier|static
name|void
name|updateLocation
parameter_list|(
specifier|final
name|CatalogTracker
name|catalogTracker
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|ServerName
name|sn
parameter_list|,
name|long
name|openSeqNum
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|addLocation
argument_list|(
name|put
argument_list|,
name|sn
argument_list|,
name|openSeqNum
argument_list|)
expr_stmt|;
name|putToCatalogTable
argument_list|(
name|catalogTracker
argument_list|,
name|put
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Updated row "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" with server="
operator|+
name|sn
argument_list|)
expr_stmt|;
block|}
comment|/**    * Deletes the specified region from META.    * @param catalogTracker    * @param regionInfo region to be deleted from META    * @throws IOException    */
specifier|public
specifier|static
name|void
name|deleteRegion
parameter_list|(
name|CatalogTracker
name|catalogTracker
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|deleteFromMetaTable
argument_list|(
name|catalogTracker
argument_list|,
name|delete
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleted "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Deletes the specified regions from META.    * @param catalogTracker    * @param regionsInfo list of regions to be deleted from META    * @throws IOException    */
specifier|public
specifier|static
name|void
name|deleteRegions
parameter_list|(
name|CatalogTracker
name|catalogTracker
parameter_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionsInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Delete
argument_list|>
name|deletes
init|=
operator|new
name|ArrayList
argument_list|<
name|Delete
argument_list|>
argument_list|(
name|regionsInfo
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|regionsInfo
control|)
block|{
name|deletes
operator|.
name|add
argument_list|(
operator|new
name|Delete
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|deleteFromMetaTable
argument_list|(
name|catalogTracker
argument_list|,
name|deletes
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleted "
operator|+
name|regionsInfo
argument_list|)
expr_stmt|;
block|}
comment|/**    * Adds and Removes the specified regions from .META.    * @param catalogTracker    * @param regionsToRemove list of regions to be deleted from META    * @param regionsToAdd list of regions to be added to META    * @throws IOException    */
specifier|public
specifier|static
name|void
name|mutateRegions
parameter_list|(
name|CatalogTracker
name|catalogTracker
parameter_list|,
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionsToRemove
parameter_list|,
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionsToAdd
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Mutation
argument_list|>
name|mutation
init|=
operator|new
name|ArrayList
argument_list|<
name|Mutation
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionsToRemove
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|regionsToRemove
control|)
block|{
name|mutation
operator|.
name|add
argument_list|(
operator|new
name|Delete
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|regionsToAdd
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|regionsToAdd
control|)
block|{
name|mutation
operator|.
name|add
argument_list|(
name|makePutFromRegionInfo
argument_list|(
name|hri
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|mutateMetaTable
argument_list|(
name|catalogTracker
argument_list|,
name|mutation
argument_list|)
expr_stmt|;
if|if
condition|(
name|regionsToRemove
operator|!=
literal|null
operator|&&
name|regionsToRemove
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deleted "
operator|+
name|regionsToRemove
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|regionsToAdd
operator|!=
literal|null
operator|&&
name|regionsToAdd
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Added "
operator|+
name|regionsToAdd
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Deletes merge qualifiers for the specified merged region.    * @param catalogTracker    * @param mergedRegion    * @throws IOException    */
specifier|public
specifier|static
name|void
name|deleteMergeQualifiers
parameter_list|(
name|CatalogTracker
name|catalogTracker
parameter_list|,
specifier|final
name|HRegionInfo
name|mergedRegion
parameter_list|)
throws|throws
name|IOException
block|{
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|mergedRegion
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|delete
operator|.
name|deleteColumns
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|MERGEA_QUALIFIER
argument_list|)
expr_stmt|;
name|delete
operator|.
name|deleteColumns
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|MERGEB_QUALIFIER
argument_list|)
expr_stmt|;
name|deleteFromMetaTable
argument_list|(
name|catalogTracker
argument_list|,
name|delete
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleted references in merged region "
operator|+
name|mergedRegion
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|", qualifier="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|HConstants
operator|.
name|MERGEA_QUALIFIER
argument_list|)
operator|+
literal|" and qualifier="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|HConstants
operator|.
name|MERGEB_QUALIFIER
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|Put
name|addRegionInfo
parameter_list|(
specifier|final
name|Put
name|p
parameter_list|,
specifier|final
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|IOException
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
name|REGIONINFO_QUALIFIER
argument_list|,
name|hri
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|p
return|;
block|}
specifier|private
specifier|static
name|Put
name|addLocation
parameter_list|(
specifier|final
name|Put
name|p
parameter_list|,
specifier|final
name|ServerName
name|sn
parameter_list|,
name|long
name|openSeqNum
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
name|SERVER_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|sn
operator|.
name|getHostAndPort
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
name|STARTCODE_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|sn
operator|.
name|getStartcode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
name|SEQNUM_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|openSeqNum
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|p
return|;
block|}
block|}
end_class

end_unit

