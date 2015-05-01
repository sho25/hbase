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
name|regionserver
operator|.
name|handler
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
name|hbase
operator|.
name|executor
operator|.
name|EventHandler
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
name|executor
operator|.
name|EventType
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
name|Region
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
name|RegionServerServices
import|;
end_import

begin_class
specifier|public
class|class
name|FinishRegionRecoveringHandler
extends|extends
name|EventHandler
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
name|FinishRegionRecoveringHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|RegionServerServices
name|rss
decl_stmt|;
specifier|protected
specifier|final
name|String
name|regionName
decl_stmt|;
specifier|protected
specifier|final
name|String
name|path
decl_stmt|;
specifier|public
name|FinishRegionRecoveringHandler
parameter_list|(
name|RegionServerServices
name|rss
parameter_list|,
name|String
name|regionName
parameter_list|,
name|String
name|path
parameter_list|)
block|{
comment|// we are using the open region handlers, since this operation is in the region open lifecycle
name|super
argument_list|(
name|rss
argument_list|,
name|EventType
operator|.
name|M_RS_OPEN_REGION
argument_list|)
expr_stmt|;
name|this
operator|.
name|rss
operator|=
name|rss
expr_stmt|;
name|this
operator|.
name|regionName
operator|=
name|regionName
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
throws|throws
name|IOException
block|{
name|Region
name|region
init|=
name|this
operator|.
name|rss
operator|.
name|getRecoveringRegions
argument_list|()
operator|.
name|remove
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
operator|(
operator|(
name|HRegion
operator|)
name|region
operator|)
operator|.
name|setRecovering
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|path
operator|+
literal|" deleted; "
operator|+
name|regionName
operator|+
literal|" recovered."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

