begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|// Generated by the protocol buffer compiler.  DO NOT EDIT!
end_comment

begin_comment
comment|// source: google/protobuf/struct.proto
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
package|;
end_package

begin_class
specifier|public
specifier|final
class|class
name|StructProto
block|{
specifier|private
name|StructProto
parameter_list|()
block|{}
specifier|public
specifier|static
name|void
name|registerAllExtensions
parameter_list|(
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
name|protobuf
operator|.
name|ExtensionRegistryLite
name|registry
parameter_list|)
block|{   }
specifier|public
specifier|static
name|void
name|registerAllExtensions
parameter_list|(
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
name|protobuf
operator|.
name|ExtensionRegistry
name|registry
parameter_list|)
block|{
name|registerAllExtensions
argument_list|(
operator|(
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
name|protobuf
operator|.
name|ExtensionRegistryLite
operator|)
name|registry
argument_list|)
expr_stmt|;
block|}
specifier|static
specifier|final
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
name|protobuf
operator|.
name|Descriptors
operator|.
name|Descriptor
name|internal_static_google_protobuf_Struct_descriptor
decl_stmt|;
specifier|static
specifier|final
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
name|protobuf
operator|.
name|GeneratedMessageV3
operator|.
name|FieldAccessorTable
name|internal_static_google_protobuf_Struct_fieldAccessorTable
decl_stmt|;
specifier|static
specifier|final
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
name|protobuf
operator|.
name|Descriptors
operator|.
name|Descriptor
name|internal_static_google_protobuf_Struct_FieldsEntry_descriptor
decl_stmt|;
specifier|static
specifier|final
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
name|protobuf
operator|.
name|GeneratedMessageV3
operator|.
name|FieldAccessorTable
name|internal_static_google_protobuf_Struct_FieldsEntry_fieldAccessorTable
decl_stmt|;
specifier|static
specifier|final
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
name|protobuf
operator|.
name|Descriptors
operator|.
name|Descriptor
name|internal_static_google_protobuf_Value_descriptor
decl_stmt|;
specifier|static
specifier|final
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
name|protobuf
operator|.
name|GeneratedMessageV3
operator|.
name|FieldAccessorTable
name|internal_static_google_protobuf_Value_fieldAccessorTable
decl_stmt|;
specifier|static
specifier|final
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
name|protobuf
operator|.
name|Descriptors
operator|.
name|Descriptor
name|internal_static_google_protobuf_ListValue_descriptor
decl_stmt|;
specifier|static
specifier|final
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
name|protobuf
operator|.
name|GeneratedMessageV3
operator|.
name|FieldAccessorTable
name|internal_static_google_protobuf_ListValue_fieldAccessorTable
decl_stmt|;
specifier|public
specifier|static
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
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
name|getDescriptor
parameter_list|()
block|{
return|return
name|descriptor
return|;
block|}
specifier|private
specifier|static
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
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
name|descriptor
decl_stmt|;
static|static
block|{
name|java
operator|.
name|lang
operator|.
name|String
index|[]
name|descriptorData
init|=
block|{
literal|"\n\034google/protobuf/struct.proto\022\017google.p"
operator|+
literal|"rotobuf\"\204\001\n\006Struct\0223\n\006fields\030\001 \003(\0132#.goo"
operator|+
literal|"gle.protobuf.Struct.FieldsEntry\032E\n\013Field"
operator|+
literal|"sEntry\022\013\n\003key\030\001 \001(\t\022%\n\005value\030\002 \001(\0132\026.goo"
operator|+
literal|"gle.protobuf.Value:\0028\001\"\352\001\n\005Value\0220\n\nnull"
operator|+
literal|"_value\030\001 \001(\0162\032.google.protobuf.NullValue"
operator|+
literal|"H\000\022\026\n\014number_value\030\002 \001(\001H\000\022\026\n\014string_val"
operator|+
literal|"ue\030\003 \001(\tH\000\022\024\n\nbool_value\030\004 \001(\010H\000\022/\n\014stru"
operator|+
literal|"ct_value\030\005 \001(\0132\027.google.protobuf.StructH"
operator|+
literal|"\000\0220\n\nlist_value\030\006 \001(\0132\032.google.protobuf."
block|,
literal|"ListValueH\000B\006\n\004kind\"3\n\tListValue\022&\n\006valu"
operator|+
literal|"es\030\001 \003(\0132\026.google.protobuf.Value*\033\n\tNull"
operator|+
literal|"Value\022\016\n\nNULL_VALUE\020\000B\201\001\n\023com.google.pro"
operator|+
literal|"tobufB\013StructProtoP\001Z1github.com/golang/"
operator|+
literal|"protobuf/ptypes/struct;structpb\370\001\001\242\002\003GPB"
operator|+
literal|"\252\002\036Google.Protobuf.WellKnownTypesb\006proto"
operator|+
literal|"3"
block|}
decl_stmt|;
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
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
operator|.
name|InternalDescriptorAssigner
name|assigner
init|=
operator|new
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
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
operator|.
name|InternalDescriptorAssigner
argument_list|()
block|{
specifier|public
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
name|protobuf
operator|.
name|ExtensionRegistry
name|assignDescriptors
parameter_list|(
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
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
name|root
parameter_list|)
block|{
name|descriptor
operator|=
name|root
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
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
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
operator|.
name|internalBuildGeneratedFileFrom
argument_list|(
name|descriptorData
argument_list|,
operator|new
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
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
index|[]
block|{         }
argument_list|,
name|assigner
argument_list|)
expr_stmt|;
name|internal_static_google_protobuf_Struct_descriptor
operator|=
name|getDescriptor
argument_list|()
operator|.
name|getMessageTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|internal_static_google_protobuf_Struct_fieldAccessorTable
operator|=
operator|new
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
name|protobuf
operator|.
name|GeneratedMessageV3
operator|.
name|FieldAccessorTable
argument_list|(
name|internal_static_google_protobuf_Struct_descriptor
argument_list|,
operator|new
name|java
operator|.
name|lang
operator|.
name|String
index|[]
block|{
literal|"Fields"
block|, }
argument_list|)
expr_stmt|;
name|internal_static_google_protobuf_Struct_FieldsEntry_descriptor
operator|=
name|internal_static_google_protobuf_Struct_descriptor
operator|.
name|getNestedTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|internal_static_google_protobuf_Struct_FieldsEntry_fieldAccessorTable
operator|=
operator|new
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
name|protobuf
operator|.
name|GeneratedMessageV3
operator|.
name|FieldAccessorTable
argument_list|(
name|internal_static_google_protobuf_Struct_FieldsEntry_descriptor
argument_list|,
operator|new
name|java
operator|.
name|lang
operator|.
name|String
index|[]
block|{
literal|"Key"
block|,
literal|"Value"
block|, }
argument_list|)
expr_stmt|;
name|internal_static_google_protobuf_Value_descriptor
operator|=
name|getDescriptor
argument_list|()
operator|.
name|getMessageTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|internal_static_google_protobuf_Value_fieldAccessorTable
operator|=
operator|new
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
name|protobuf
operator|.
name|GeneratedMessageV3
operator|.
name|FieldAccessorTable
argument_list|(
name|internal_static_google_protobuf_Value_descriptor
argument_list|,
operator|new
name|java
operator|.
name|lang
operator|.
name|String
index|[]
block|{
literal|"NullValue"
block|,
literal|"NumberValue"
block|,
literal|"StringValue"
block|,
literal|"BoolValue"
block|,
literal|"StructValue"
block|,
literal|"ListValue"
block|,
literal|"Kind"
block|, }
argument_list|)
expr_stmt|;
name|internal_static_google_protobuf_ListValue_descriptor
operator|=
name|getDescriptor
argument_list|()
operator|.
name|getMessageTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|internal_static_google_protobuf_ListValue_fieldAccessorTable
operator|=
operator|new
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
name|protobuf
operator|.
name|GeneratedMessageV3
operator|.
name|FieldAccessorTable
argument_list|(
name|internal_static_google_protobuf_ListValue_descriptor
argument_list|,
operator|new
name|java
operator|.
name|lang
operator|.
name|String
index|[]
block|{
literal|"Values"
block|, }
argument_list|)
expr_stmt|;
block|}
comment|// @@protoc_insertion_point(outer_class_scope)
block|}
end_class

end_unit

