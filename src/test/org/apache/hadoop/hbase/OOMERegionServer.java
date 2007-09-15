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
name|io
operator|.
name|BatchUpdate
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * A region server that will OOME.  * Everytime {@link #batchUpdate(Text, long, BatchUpdate)} is called, we add  * keep around a reference to the batch.  Use this class to test OOME extremes.  * Needs to be started manually as in  *<code>${HBASE_HOME}/bin/hbase ./bin/hbase org.apache.hadoop.hbase.OOMERegionServer start</code>.  */
end_comment

begin_class
specifier|public
class|class
name|OOMERegionServer
extends|extends
name|HRegionServer
block|{
specifier|private
name|List
argument_list|<
name|BatchUpdate
argument_list|>
name|retainer
init|=
operator|new
name|ArrayList
argument_list|<
name|BatchUpdate
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|OOMERegionServer
parameter_list|(
name|Configuration
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
name|OOMERegionServer
parameter_list|(
name|Path
name|rootDir
parameter_list|,
name|HServerAddress
name|address
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|rootDir
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
name|void
name|batchUpdate
parameter_list|(
name|Text
name|regionName
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|BatchUpdate
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|batchUpdate
argument_list|(
name|regionName
argument_list|,
name|timestamp
argument_list|,
name|b
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|30
condition|;
name|i
operator|++
control|)
block|{
comment|// Add the batch update 30 times to bring on the OOME faster.
name|this
operator|.
name|retainer
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
block|}
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
name|HRegionServer
operator|.
name|doMain
argument_list|(
name|args
argument_list|,
name|OOMERegionServer
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

