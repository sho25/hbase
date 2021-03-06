begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|Strings
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|protobuf
operator|.
name|generated
operator|.
name|ClusterStatusProtos
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|UserMetricsBuilder
block|{
specifier|public
specifier|static
name|UserMetrics
name|toUserMetrics
parameter_list|(
name|ClusterStatusProtos
operator|.
name|UserLoad
name|userLoad
parameter_list|)
block|{
name|UserMetricsBuilder
name|builder
init|=
name|UserMetricsBuilder
operator|.
name|newBuilder
argument_list|(
name|userLoad
operator|.
name|getUserName
argument_list|()
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|userLoad
operator|.
name|getClientMetricsList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|clientMetrics
lambda|->
operator|new
name|ClientMetricsImpl
argument_list|(
name|clientMetrics
operator|.
name|getHostName
argument_list|()
argument_list|,
name|clientMetrics
operator|.
name|getReadRequestsCount
argument_list|()
argument_list|,
name|clientMetrics
operator|.
name|getWriteRequestsCount
argument_list|()
argument_list|,
name|clientMetrics
operator|.
name|getFilteredRequestsCount
argument_list|()
argument_list|)
argument_list|)
operator|.
name|forEach
argument_list|(
name|builder
operator|::
name|addClientMetris
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|ClusterStatusProtos
operator|.
name|UserLoad
name|toUserMetrics
parameter_list|(
name|UserMetrics
name|userMetrics
parameter_list|)
block|{
name|ClusterStatusProtos
operator|.
name|UserLoad
operator|.
name|Builder
name|builder
init|=
name|ClusterStatusProtos
operator|.
name|UserLoad
operator|.
name|newBuilder
argument_list|()
operator|.
name|setUserName
argument_list|(
name|userMetrics
operator|.
name|getNameAsString
argument_list|()
argument_list|)
decl_stmt|;
name|userMetrics
operator|.
name|getClientMetrics
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|clientMetrics
lambda|->
name|ClusterStatusProtos
operator|.
name|ClientMetrics
operator|.
name|newBuilder
argument_list|()
operator|.
name|setHostName
argument_list|(
name|clientMetrics
operator|.
name|getHostName
argument_list|()
argument_list|)
operator|.
name|setWriteRequestsCount
argument_list|(
name|clientMetrics
operator|.
name|getWriteRequestsCount
argument_list|()
argument_list|)
operator|.
name|setReadRequestsCount
argument_list|(
name|clientMetrics
operator|.
name|getReadRequestsCount
argument_list|()
argument_list|)
operator|.
name|setFilteredRequestsCount
argument_list|(
name|clientMetrics
operator|.
name|getFilteredReadRequestsCount
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|forEach
argument_list|(
name|builder
operator|::
name|addClientMetrics
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|UserMetricsBuilder
name|newBuilder
parameter_list|(
name|byte
index|[]
name|name
parameter_list|)
block|{
return|return
operator|new
name|UserMetricsBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|private
specifier|final
name|byte
index|[]
name|name
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|UserMetrics
operator|.
name|ClientMetrics
argument_list|>
name|clientMetricsMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|UserMetricsBuilder
parameter_list|(
name|byte
index|[]
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|UserMetricsBuilder
name|addClientMetris
parameter_list|(
name|UserMetrics
operator|.
name|ClientMetrics
name|clientMetrics
parameter_list|)
block|{
name|clientMetricsMap
operator|.
name|put
argument_list|(
name|clientMetrics
operator|.
name|getHostName
argument_list|()
argument_list|,
name|clientMetrics
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|UserMetrics
name|build
parameter_list|()
block|{
return|return
operator|new
name|UserMetricsImpl
argument_list|(
name|name
argument_list|,
name|clientMetricsMap
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|ClientMetricsImpl
implements|implements
name|UserMetrics
operator|.
name|ClientMetrics
block|{
specifier|private
specifier|final
name|long
name|filteredReadRequestsCount
decl_stmt|;
specifier|private
specifier|final
name|String
name|hostName
decl_stmt|;
specifier|private
specifier|final
name|long
name|readRequestCount
decl_stmt|;
specifier|private
specifier|final
name|long
name|writeRequestCount
decl_stmt|;
specifier|public
name|ClientMetricsImpl
parameter_list|(
name|String
name|hostName
parameter_list|,
name|long
name|readRequest
parameter_list|,
name|long
name|writeRequest
parameter_list|,
name|long
name|filteredReadRequestsCount
parameter_list|)
block|{
name|this
operator|.
name|hostName
operator|=
name|hostName
expr_stmt|;
name|this
operator|.
name|readRequestCount
operator|=
name|readRequest
expr_stmt|;
name|this
operator|.
name|writeRequestCount
operator|=
name|writeRequest
expr_stmt|;
name|this
operator|.
name|filteredReadRequestsCount
operator|=
name|filteredReadRequestsCount
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getHostName
parameter_list|()
block|{
return|return
name|hostName
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getReadRequestsCount
parameter_list|()
block|{
return|return
name|readRequestCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getWriteRequestsCount
parameter_list|()
block|{
return|return
name|writeRequestCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getFilteredReadRequestsCount
parameter_list|()
block|{
return|return
name|filteredReadRequestsCount
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|UserMetricsImpl
implements|implements
name|UserMetrics
block|{
specifier|private
specifier|final
name|byte
index|[]
name|name
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ClientMetrics
argument_list|>
name|clientMetricsMap
decl_stmt|;
name|UserMetricsImpl
parameter_list|(
name|byte
index|[]
name|name
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ClientMetrics
argument_list|>
name|clientMetricsMap
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|clientMetricsMap
operator|=
name|clientMetricsMap
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getUserName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getReadRequestCount
parameter_list|()
block|{
return|return
name|clientMetricsMap
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|c
lambda|->
name|c
operator|.
name|getReadRequestsCount
argument_list|()
argument_list|)
operator|.
name|reduce
argument_list|(
literal|0L
argument_list|,
name|Long
operator|::
name|sum
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getWriteRequestCount
parameter_list|()
block|{
return|return
name|clientMetricsMap
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|c
lambda|->
name|c
operator|.
name|getWriteRequestsCount
argument_list|()
argument_list|)
operator|.
name|reduce
argument_list|(
literal|0L
argument_list|,
name|Long
operator|::
name|sum
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ClientMetrics
argument_list|>
name|getClientMetrics
parameter_list|()
block|{
return|return
name|this
operator|.
name|clientMetricsMap
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getFilteredReadRequests
parameter_list|()
block|{
return|return
name|clientMetricsMap
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|c
lambda|->
name|c
operator|.
name|getFilteredReadRequestsCount
argument_list|()
argument_list|)
operator|.
name|reduce
argument_list|(
literal|0L
argument_list|,
name|Long
operator|::
name|sum
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
name|Strings
operator|.
name|appendKeyValue
argument_list|(
operator|new
name|StringBuilder
argument_list|()
argument_list|,
literal|"readRequestCount"
argument_list|,
name|this
operator|.
name|getReadRequestCount
argument_list|()
argument_list|)
decl_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"writeRequestCount"
argument_list|,
name|this
operator|.
name|getWriteRequestCount
argument_list|()
argument_list|)
expr_stmt|;
name|Strings
operator|.
name|appendKeyValue
argument_list|(
name|sb
argument_list|,
literal|"filteredReadRequestCount"
argument_list|,
name|this
operator|.
name|getFilteredReadRequests
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

