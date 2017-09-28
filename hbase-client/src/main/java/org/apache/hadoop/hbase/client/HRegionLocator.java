begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/** * * Licensed to the Apache Software Foundation (ASF) under one * or more contributor license agreements.  See the NOTICE file * distributed with this work for additional information * regarding copyright ownership.  The ASF licenses this file * to you under the Apache License, Version 2.0 (the * "License"); you may not use this file except in compliance * with the License.  You may obtain a copy of the License at * *     http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */
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
name|client
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
name|HRegionLocation
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
name|MetaTableAccessor
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
name|RegionLocations
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
name|util
operator|.
name|Pair
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
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * An implementation of {@link RegionLocator}. Used to view region location information for a single  * HBase table. Lightweight. Get as needed and just close when done. Instances of this class SHOULD  * NOT be constructed directly. Obtain an instance via {@link Connection}. See  * {@link ConnectionFactory} class comment for an example of how.  *  *<p> This class is thread safe  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|HRegionLocator
implements|implements
name|RegionLocator
block|{
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|ClusterConnection
name|connection
decl_stmt|;
specifier|public
name|HRegionLocator
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|ClusterConnection
name|connection
parameter_list|)
block|{
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
comment|// This method is required by the RegionLocator interface. This implementation does not have any
comment|// persistent state, so there is no need to do anything here.
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|HRegionLocation
name|getRegionLocation
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|connection
operator|.
name|getRegionLocation
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|HRegionLocation
name|getRegionLocation
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|connection
operator|.
name|getRegionLocation
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|reload
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|getAllRegionLocations
parameter_list|()
throws|throws
name|IOException
block|{
name|TableName
name|tableName
init|=
name|getName
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|RegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|>
name|locations
init|=
name|MetaTableAccessor
operator|.
name|getTableRegionsAndLocations
argument_list|(
name|this
operator|.
name|connection
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|HRegionLocation
argument_list|>
name|regions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|locations
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|RegionInfo
argument_list|,
name|ServerName
argument_list|>
name|entry
range|:
name|locations
control|)
block|{
name|regions
operator|.
name|add
argument_list|(
operator|new
name|HRegionLocation
argument_list|(
name|entry
operator|.
name|getFirst
argument_list|()
argument_list|,
name|entry
operator|.
name|getSecond
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|regions
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|connection
operator|.
name|cacheLocation
argument_list|(
name|tableName
argument_list|,
operator|new
name|RegionLocations
argument_list|(
name|regions
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|regions
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
index|[]
name|getStartKeys
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getStartEndKeys
argument_list|()
operator|.
name|getFirst
argument_list|()
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
index|[]
name|getEndKeys
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getStartEndKeys
argument_list|()
operator|.
name|getSecond
argument_list|()
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|Pair
argument_list|<
name|byte
index|[]
index|[]
argument_list|,
name|byte
index|[]
index|[]
argument_list|>
name|getStartEndKeys
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getStartEndKeys
argument_list|(
name|listRegionLocations
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
name|Pair
argument_list|<
name|byte
index|[]
index|[]
argument_list|,
name|byte
index|[]
index|[]
argument_list|>
name|getStartEndKeys
parameter_list|(
name|List
argument_list|<
name|RegionLocations
argument_list|>
name|regions
parameter_list|)
block|{
specifier|final
name|byte
index|[]
index|[]
name|startKeyList
init|=
operator|new
name|byte
index|[
name|regions
operator|.
name|size
argument_list|()
index|]
index|[]
decl_stmt|;
specifier|final
name|byte
index|[]
index|[]
name|endKeyList
init|=
operator|new
name|byte
index|[
name|regions
operator|.
name|size
argument_list|()
index|]
index|[]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|regions
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|HRegionInfo
name|region
init|=
name|regions
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getRegionLocation
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|startKeyList
index|[
name|i
index|]
operator|=
name|region
operator|.
name|getStartKey
argument_list|()
expr_stmt|;
name|endKeyList
index|[
name|i
index|]
operator|=
name|region
operator|.
name|getEndKey
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|startKeyList
argument_list|,
name|endKeyList
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableName
name|getName
parameter_list|()
block|{
return|return
name|this
operator|.
name|tableName
return|;
block|}
annotation|@
name|VisibleForTesting
name|List
argument_list|<
name|RegionLocations
argument_list|>
name|listRegionLocations
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|List
argument_list|<
name|RegionLocations
argument_list|>
name|regions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|MetaTableAccessor
operator|.
name|Visitor
name|visitor
init|=
operator|new
name|MetaTableAccessor
operator|.
name|TableVisitorBase
argument_list|(
name|tableName
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|visitInternal
parameter_list|(
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
block|{
name|RegionLocations
name|locations
init|=
name|MetaTableAccessor
operator|.
name|getRegionLocations
argument_list|(
name|result
argument_list|)
decl_stmt|;
if|if
condition|(
name|locations
operator|==
literal|null
condition|)
return|return
literal|true
return|;
name|regions
operator|.
name|add
argument_list|(
name|locations
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
decl_stmt|;
name|MetaTableAccessor
operator|.
name|scanMetaForTableRegions
argument_list|(
name|connection
argument_list|,
name|visitor
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
return|return
name|regions
return|;
block|}
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|connection
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
block|}
end_class

end_unit

