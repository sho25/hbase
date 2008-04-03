begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HBaseConfiguration
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
name|HServerAddress
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
name|HMsg
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

begin_comment
comment|/**  * An HMaster that runs out of memory.  * Everytime a region server reports in, add to the retained heap of memory.  * Needs to be started manually as in  *<code>${HBASE_HOME}/bin/hbase ./bin/hbase org.apache.hadoop.hbase.OOMEHMaster start/code>.  */
end_comment

begin_class
specifier|public
class|class
name|OOMEHMaster
extends|extends
name|HMaster
block|{
specifier|private
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|retainer
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|OOMEHMaster
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|OOMEHMaster
parameter_list|(
name|Path
name|dir
parameter_list|,
name|HServerAddress
name|address
parameter_list|,
name|HBaseConfiguration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|dir
argument_list|,
name|address
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HMsg
index|[]
name|regionServerReport
parameter_list|(
name|HServerInfo
name|serverInfo
parameter_list|,
name|HMsg
index|[]
name|msgs
parameter_list|,
name|HRegionInfo
index|[]
name|mostLoadedRegions
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Retain 1M.
name|this
operator|.
name|retainer
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[
literal|1024
operator|*
literal|1024
index|]
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|regionServerReport
argument_list|(
name|serverInfo
argument_list|,
name|msgs
argument_list|,
name|mostLoadedRegions
argument_list|)
return|;
block|}
comment|/**    * @param args    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|doMain
argument_list|(
name|args
argument_list|,
name|OOMEHMaster
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

