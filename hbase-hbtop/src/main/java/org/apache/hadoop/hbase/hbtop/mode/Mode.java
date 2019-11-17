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
name|hbtop
operator|.
name|mode
package|;
end_package

begin_import
import|import
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|Nullable
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
name|Objects
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
name|ClusterMetrics
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
name|hbtop
operator|.
name|Record
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
name|hbtop
operator|.
name|RecordFilter
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
name|hbtop
operator|.
name|field
operator|.
name|Field
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
name|hbtop
operator|.
name|field
operator|.
name|FieldInfo
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

begin_comment
comment|/**  * Represents a display mode in the top screen.  */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
enum|enum
name|Mode
block|{
name|NAMESPACE
argument_list|(
literal|"Namespace"
argument_list|,
literal|"Record per Namespace"
argument_list|,
operator|new
name|NamespaceModeStrategy
argument_list|()
argument_list|)
block|,
name|TABLE
argument_list|(
literal|"Table"
argument_list|,
literal|"Record per Table"
argument_list|,
operator|new
name|TableModeStrategy
argument_list|()
argument_list|)
block|,
name|REGION
argument_list|(
literal|"Region"
argument_list|,
literal|"Record per Region"
argument_list|,
operator|new
name|RegionModeStrategy
argument_list|()
argument_list|)
block|,
name|REGION_SERVER
argument_list|(
literal|"RegionServer"
argument_list|,
literal|"Record per RegionServer"
argument_list|,
operator|new
name|RegionServerModeStrategy
argument_list|()
argument_list|)
block|,
name|USER
argument_list|(
literal|"User"
argument_list|,
literal|"Record per user"
argument_list|,
operator|new
name|UserModeStrategy
argument_list|()
argument_list|)
block|,
name|CLIENT
argument_list|(
literal|"Client"
argument_list|,
literal|"Record per client"
argument_list|,
operator|new
name|ClientModeStrategy
argument_list|()
argument_list|)
block|;
specifier|private
specifier|final
name|String
name|header
decl_stmt|;
specifier|private
specifier|final
name|String
name|description
decl_stmt|;
specifier|private
specifier|final
name|ModeStrategy
name|modeStrategy
decl_stmt|;
name|Mode
parameter_list|(
name|String
name|header
parameter_list|,
name|String
name|description
parameter_list|,
name|ModeStrategy
name|modeStrategy
parameter_list|)
block|{
name|this
operator|.
name|header
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|header
argument_list|)
expr_stmt|;
name|this
operator|.
name|description
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|description
argument_list|)
expr_stmt|;
name|this
operator|.
name|modeStrategy
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|modeStrategy
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getHeader
parameter_list|()
block|{
return|return
name|header
return|;
block|}
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
return|return
name|description
return|;
block|}
specifier|public
name|List
argument_list|<
name|Record
argument_list|>
name|getRecords
parameter_list|(
name|ClusterMetrics
name|clusterMetrics
parameter_list|,
name|List
argument_list|<
name|RecordFilter
argument_list|>
name|pushDownFilters
parameter_list|)
block|{
return|return
name|modeStrategy
operator|.
name|getRecords
argument_list|(
name|clusterMetrics
argument_list|,
name|pushDownFilters
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|FieldInfo
argument_list|>
name|getFieldInfos
parameter_list|()
block|{
return|return
name|modeStrategy
operator|.
name|getFieldInfos
argument_list|()
return|;
block|}
specifier|public
name|Field
name|getDefaultSortField
parameter_list|()
block|{
return|return
name|modeStrategy
operator|.
name|getDefaultSortField
argument_list|()
return|;
block|}
annotation|@
name|Nullable
specifier|public
name|DrillDownInfo
name|drillDown
parameter_list|(
name|Record
name|currentRecord
parameter_list|)
block|{
return|return
name|modeStrategy
operator|.
name|drillDown
argument_list|(
name|currentRecord
argument_list|)
return|;
block|}
block|}
end_enum

end_unit

