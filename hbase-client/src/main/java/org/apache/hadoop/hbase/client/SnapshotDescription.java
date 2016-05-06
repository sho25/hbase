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
name|client
package|;
end_package

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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * The POJO equivalent of HBaseProtos.SnapshotDescription  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|SnapshotDescription
block|{
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|String
name|table
decl_stmt|;
specifier|private
name|SnapshotType
name|snapShotType
init|=
name|SnapshotType
operator|.
name|DISABLED
decl_stmt|;
specifier|private
name|String
name|owner
decl_stmt|;
specifier|private
name|long
name|creationTime
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
name|int
name|version
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
name|SnapshotDescription
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SnapshotDescription
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|table
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|table
argument_list|,
name|SnapshotType
operator|.
name|DISABLED
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SnapshotDescription
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|table
parameter_list|,
name|SnapshotType
name|type
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|table
argument_list|,
name|type
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SnapshotDescription
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|table
parameter_list|,
name|SnapshotType
name|type
parameter_list|,
name|String
name|owner
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|table
argument_list|,
name|type
argument_list|,
name|owner
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SnapshotDescription
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|table
parameter_list|,
name|SnapshotType
name|type
parameter_list|,
name|String
name|owner
parameter_list|,
name|long
name|creationTime
parameter_list|,
name|int
name|version
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|snapShotType
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|owner
operator|=
name|owner
expr_stmt|;
name|this
operator|.
name|creationTime
operator|=
name|creationTime
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
specifier|public
name|String
name|getTable
parameter_list|()
block|{
return|return
name|this
operator|.
name|table
return|;
block|}
specifier|public
name|SnapshotType
name|getType
parameter_list|()
block|{
return|return
name|this
operator|.
name|snapShotType
return|;
block|}
specifier|public
name|String
name|getOwner
parameter_list|()
block|{
return|return
name|this
operator|.
name|owner
return|;
block|}
specifier|public
name|long
name|getCreationTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|creationTime
return|;
block|}
specifier|public
name|int
name|getVersion
parameter_list|()
block|{
return|return
name|this
operator|.
name|version
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"SnapshotDescription: name = "
operator|+
operator|(
operator|(
name|name
operator|!=
literal|null
operator|)
condition|?
name|name
else|:
literal|null
operator|)
operator|+
literal|"/table = "
operator|+
operator|(
operator|(
name|table
operator|!=
literal|null
operator|)
condition|?
name|table
else|:
literal|null
operator|)
operator|+
literal|" /owner = "
operator|+
operator|(
operator|(
name|owner
operator|!=
literal|null
operator|)
condition|?
name|owner
else|:
literal|null
operator|)
operator|+
operator|(
name|creationTime
operator|!=
operator|-
literal|1
condition|?
operator|(
literal|"/creationtime = "
operator|+
name|creationTime
operator|)
else|:
literal|""
operator|)
operator|+
operator|(
name|version
operator|!=
operator|-
literal|1
condition|?
operator|(
literal|"/version = "
operator|+
name|version
operator|)
else|:
literal|""
operator|)
return|;
block|}
block|}
end_class

end_unit

