begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|net
operator|.
name|ConnectException
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
name|HServerInfo
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
name|HRegionInterface
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
comment|/**  * Writes region and assignment information to<code>.META.</code>.  *<p>  * Uses the {@link CatalogTracker} to obtain locations and connections to  * catalogs.  */
end_comment

begin_class
specifier|public
class|class
name|MetaEditor
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
name|MetaEditor
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Adds a META row for the specified new region.    * @param info region information    * @throws IOException if problem connecting or updating meta    */
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
name|REGIONINFO_QUALIFIER
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|regionInfo
argument_list|)
argument_list|)
expr_stmt|;
name|catalogTracker
operator|.
name|waitForMetaServerConnectionDefault
argument_list|()
operator|.
name|put
argument_list|(
name|CatalogTracker
operator|.
name|META_REGION
argument_list|,
name|put
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Added region "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" to META"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Offline parent in meta.    * Used when splitting.    * @param catalogTracker    * @param parent    * @param a Split daughter region A    * @param b Split daughter region B    * @throws NotAllMetaRegionsOnlineException    * @throws IOException    */
specifier|public
specifier|static
name|void
name|offlineParentInMeta
parameter_list|(
name|CatalogTracker
name|catalogTracker
parameter_list|,
name|HRegionInfo
name|parent
parameter_list|,
specifier|final
name|HRegionInfo
name|a
parameter_list|,
specifier|final
name|HRegionInfo
name|b
parameter_list|)
throws|throws
name|NotAllMetaRegionsOnlineException
throws|,
name|IOException
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
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|copyOfParent
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|addRegionInfo
argument_list|(
name|put
argument_list|,
name|copyOfParent
argument_list|)
expr_stmt|;
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
name|SERVER_QUALIFIER
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
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
name|STARTCODE_QUALIFIER
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
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
name|Writables
operator|.
name|getBytes
argument_list|(
name|a
argument_list|)
argument_list|)
expr_stmt|;
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
name|Writables
operator|.
name|getBytes
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|catalogTracker
operator|.
name|waitForMetaServerConnectionDefault
argument_list|()
operator|.
name|put
argument_list|(
name|CatalogTracker
operator|.
name|META_REGION
argument_list|,
name|put
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Offlined parent region "
operator|+
name|parent
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" in META"
argument_list|)
expr_stmt|;
block|}
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
name|HServerInfo
name|serverInfo
parameter_list|)
throws|throws
name|NotAllMetaRegionsOnlineException
throws|,
name|IOException
block|{
name|HRegionInterface
name|server
init|=
name|catalogTracker
operator|.
name|waitForMetaServerConnectionDefault
argument_list|()
decl_stmt|;
name|byte
index|[]
name|catalogRegionName
init|=
name|CatalogTracker
operator|.
name|META_REGION
decl_stmt|;
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
name|serverInfo
operator|!=
literal|null
condition|)
name|addLocation
argument_list|(
name|put
argument_list|,
name|serverInfo
argument_list|)
expr_stmt|;
name|server
operator|.
name|put
argument_list|(
name|catalogRegionName
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
name|getRegionNameAsString
argument_list|()
operator|+
literal|" in region "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|catalogRegionName
argument_list|)
operator|+
operator|(
name|serverInfo
operator|==
literal|null
condition|?
literal|", serverInfo=null"
else|:
literal|", serverInfo="
operator|+
name|serverInfo
operator|.
name|getServerName
argument_list|()
operator|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Updates the location of the specified META region in ROOT to be the    * specified server hostname and startcode.    *<p>    * Uses passed catalog tracker to get a connection to the server hosting    * ROOT and makes edits to that region.    *    * @param catalogTracker catalog tracker    * @param regionInfo region to update location of    * @param serverInfo server the region is located on    * @throws IOException    * @throws ConnectException Usually because the regionserver carrying .META.    * is down.    * @throws NullPointerException Because no -ROOT- server connection    */
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
name|HServerInfo
name|serverInfo
parameter_list|)
throws|throws
name|IOException
throws|,
name|ConnectException
block|{
name|HRegionInterface
name|server
init|=
name|catalogTracker
operator|.
name|waitForRootServerConnectionDefault
argument_list|()
decl_stmt|;
if|if
condition|(
name|server
operator|==
literal|null
condition|)
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"No server for -ROOT-"
argument_list|)
throw|;
name|updateLocation
argument_list|(
name|server
argument_list|,
name|CatalogTracker
operator|.
name|ROOT_REGION
argument_list|,
name|regionInfo
argument_list|,
name|serverInfo
argument_list|)
expr_stmt|;
block|}
comment|/**    * Updates the location of the specified region in META to be the specified    * server hostname and startcode.    *<p>    * Uses passed catalog tracker to get a connection to the server hosting    * META and makes edits to that region.    *    * @param catalogTracker catalog tracker    * @param regionInfo region to update location of    * @param serverInfo server the region is located on    * @throws IOException    */
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
name|HServerInfo
name|serverInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|updateLocation
argument_list|(
name|catalogTracker
operator|.
name|waitForMetaServerConnectionDefault
argument_list|()
argument_list|,
name|CatalogTracker
operator|.
name|META_REGION
argument_list|,
name|regionInfo
argument_list|,
name|serverInfo
argument_list|)
expr_stmt|;
block|}
comment|/**    * Updates the location of the specified region to be the specified server.    *<p>    * Connects to the specified server which should be hosting the specified    * catalog region name to perform the edit.    *    * @param server connection to server hosting catalog region    * @param catalogRegionName name of catalog region being updated    * @param regionInfo region to update location of    * @param serverInfo server the region is located on    * @throws IOException In particular could throw {@link java.net.ConnectException}    * if the server is down on other end.    */
specifier|private
specifier|static
name|void
name|updateLocation
parameter_list|(
name|HRegionInterface
name|server
parameter_list|,
name|byte
index|[]
name|catalogRegionName
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|HServerInfo
name|serverInfo
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
name|serverInfo
argument_list|)
expr_stmt|;
name|server
operator|.
name|put
argument_list|(
name|catalogRegionName
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
literal|" in region "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|catalogRegionName
argument_list|)
operator|+
literal|" with "
operator|+
literal|"server="
operator|+
name|serverInfo
operator|.
name|getHostnamePort
argument_list|()
operator|+
literal|", "
operator|+
literal|"startcode="
operator|+
name|serverInfo
operator|.
name|getStartCode
argument_list|()
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
name|catalogTracker
operator|.
name|waitForMetaServerConnectionDefault
argument_list|()
operator|.
name|delete
argument_list|(
name|CatalogTracker
operator|.
name|META_REGION
argument_list|,
name|delete
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleted region "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" from META"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Updates the region information for the specified region in META.    * @param catalogTracker    * @param regionInfo region to be updated in META    * @throws IOException    */
specifier|public
specifier|static
name|void
name|updateRegionInfo
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
name|catalogTracker
operator|.
name|waitForMetaServerConnectionDefault
argument_list|()
operator|.
name|put
argument_list|(
name|CatalogTracker
operator|.
name|META_REGION
argument_list|,
name|put
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Updated region "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" in META"
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
name|Writables
operator|.
name|getBytes
argument_list|(
name|hri
argument_list|)
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
name|HServerInfo
name|hsi
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
name|hsi
operator|.
name|getHostnamePort
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
name|hsi
operator|.
name|getStartCode
argument_list|()
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

