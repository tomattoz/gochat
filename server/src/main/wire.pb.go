// Code generated by protoc-gen-go.
// source: wire.proto
// DO NOT EDIT!

/*
Package main is a generated protocol buffer package.

It is generated from these files:
	wire.proto

It has these top-level messages:
	Login
	Contact
	Text
	File
	Av
	Haber
*/
package main

import proto "github.com/golang/protobuf/proto"
import fmt "fmt"
import math "math"

// Reference imports to suppress errors if they are not otherwise used.
var _ = proto.Marshal
var _ = fmt.Errorf
var _ = math.Inf

// This is a compile-time assertion to ensure that this generated file
// is compatible with the proto package it is being compiled against.
// A compilation error at this line likely means your copy of the
// proto package needs to be updated.
const _ = proto.ProtoPackageIsVersion2 // please upgrade the proto package

// Identifies which field is filled in
type Haber_Which int32

const (
	Haber_LOGIN    Haber_Which = 0
	Haber_CONTACTS Haber_Which = 1
	Haber_PRESENCE Haber_Which = 2
	Haber_TEXT     Haber_Which = 3
	Haber_FILE     Haber_Which = 4
	Haber_AV       Haber_Which = 5
)

var Haber_Which_name = map[int32]string{
	0: "LOGIN",
	1: "CONTACTS",
	2: "PRESENCE",
	3: "TEXT",
	4: "FILE",
	5: "AV",
}
var Haber_Which_value = map[string]int32{
	"LOGIN":    0,
	"CONTACTS": 1,
	"PRESENCE": 2,
	"TEXT":     3,
	"FILE":     4,
	"AV":       5,
}

func (x Haber_Which) String() string {
	return proto.EnumName(Haber_Which_name, int32(x))
}
func (Haber_Which) EnumDescriptor() ([]byte, []int) { return fileDescriptor0, []int{5, 0} }

type Login struct {
	Username string `protobuf:"bytes,1,opt,name=username" json:"username,omitempty"`
}

func (m *Login) Reset()                    { *m = Login{} }
func (m *Login) String() string            { return proto.CompactTextString(m) }
func (*Login) ProtoMessage()               {}
func (*Login) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{0} }

func (m *Login) GetUsername() string {
	if m != nil {
		return m.Username
	}
	return ""
}

type Contact struct {
	Name   string `protobuf:"bytes,1,opt,name=name" json:"name,omitempty"`
	Online bool   `protobuf:"varint,2,opt,name=online" json:"online,omitempty"`
}

func (m *Contact) Reset()                    { *m = Contact{} }
func (m *Contact) String() string            { return proto.CompactTextString(m) }
func (*Contact) ProtoMessage()               {}
func (*Contact) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{1} }

func (m *Contact) GetName() string {
	if m != nil {
		return m.Name
	}
	return ""
}

func (m *Contact) GetOnline() bool {
	if m != nil {
		return m.Online
	}
	return false
}

type Text struct {
	Body string `protobuf:"bytes,1,opt,name=body" json:"body,omitempty"`
}

func (m *Text) Reset()                    { *m = Text{} }
func (m *Text) String() string            { return proto.CompactTextString(m) }
func (*Text) ProtoMessage()               {}
func (*Text) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{2} }

func (m *Text) GetBody() string {
	if m != nil {
		return m.Body
	}
	return ""
}

type File struct {
	Key  string `protobuf:"bytes,1,opt,name=key" json:"key,omitempty"`
	Data []byte `protobuf:"bytes,2,opt,name=data,proto3" json:"data,omitempty"`
}

func (m *File) Reset()                    { *m = File{} }
func (m *File) String() string            { return proto.CompactTextString(m) }
func (*File) ProtoMessage()               {}
func (*File) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{3} }

func (m *File) GetKey() string {
	if m != nil {
		return m.Key
	}
	return ""
}

func (m *File) GetData() []byte {
	if m != nil {
		return m.Data
	}
	return nil
}

type Av struct {
	Data []byte `protobuf:"bytes,1,opt,name=data,proto3" json:"data,omitempty"`
}

func (m *Av) Reset()                    { *m = Av{} }
func (m *Av) String() string            { return proto.CompactTextString(m) }
func (*Av) ProtoMessage()               {}
func (*Av) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{4} }

func (m *Av) GetData() []byte {
	if m != nil {
		return m.Data
	}
	return nil
}

type Haber struct {
	Version   uint32      `protobuf:"varint,1,opt,name=version" json:"version,omitempty"`
	SessionId string      `protobuf:"bytes,2,opt,name=sessionId" json:"sessionId,omitempty"`
	From      string      `protobuf:"bytes,3,opt,name=from" json:"from,omitempty"`
	To        string      `protobuf:"bytes,4,opt,name=to" json:"to,omitempty"`
	Which     Haber_Which `protobuf:"varint,5,opt,name=which,enum=Haber_Which" json:"which,omitempty"`
	// One of the following will be filled in
	Login    *Login     `protobuf:"bytes,101,opt,name=login" json:"login,omitempty"`
	Contacts []*Contact `protobuf:"bytes,102,rep,name=contacts" json:"contacts,omitempty"`
	Text     *Text      `protobuf:"bytes,104,opt,name=text" json:"text,omitempty"`
	Av       *Av        `protobuf:"bytes,105,opt,name=av" json:"av,omitempty"`
	File     *File      `protobuf:"bytes,106,opt,name=file" json:"file,omitempty"`
}

func (m *Haber) Reset()                    { *m = Haber{} }
func (m *Haber) String() string            { return proto.CompactTextString(m) }
func (*Haber) ProtoMessage()               {}
func (*Haber) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{5} }

func (m *Haber) GetVersion() uint32 {
	if m != nil {
		return m.Version
	}
	return 0
}

func (m *Haber) GetSessionId() string {
	if m != nil {
		return m.SessionId
	}
	return ""
}

func (m *Haber) GetFrom() string {
	if m != nil {
		return m.From
	}
	return ""
}

func (m *Haber) GetTo() string {
	if m != nil {
		return m.To
	}
	return ""
}

func (m *Haber) GetWhich() Haber_Which {
	if m != nil {
		return m.Which
	}
	return Haber_LOGIN
}

func (m *Haber) GetLogin() *Login {
	if m != nil {
		return m.Login
	}
	return nil
}

func (m *Haber) GetContacts() []*Contact {
	if m != nil {
		return m.Contacts
	}
	return nil
}

func (m *Haber) GetText() *Text {
	if m != nil {
		return m.Text
	}
	return nil
}

func (m *Haber) GetAv() *Av {
	if m != nil {
		return m.Av
	}
	return nil
}

func (m *Haber) GetFile() *File {
	if m != nil {
		return m.File
	}
	return nil
}

func init() {
	proto.RegisterType((*Login)(nil), "Login")
	proto.RegisterType((*Contact)(nil), "Contact")
	proto.RegisterType((*Text)(nil), "Text")
	proto.RegisterType((*File)(nil), "File")
	proto.RegisterType((*Av)(nil), "Av")
	proto.RegisterType((*Haber)(nil), "Haber")
	proto.RegisterEnum("Haber_Which", Haber_Which_name, Haber_Which_value)
}

func init() { proto.RegisterFile("wire.proto", fileDescriptor0) }

var fileDescriptor0 = []byte{
	// 394 bytes of a gzipped FileDescriptorProto
	0x1f, 0x8b, 0x08, 0x00, 0x00, 0x09, 0x6e, 0x88, 0x02, 0xff, 0x4c, 0x52, 0x4d, 0x6f, 0xd3, 0x30,
	0x18, 0x26, 0x8e, 0x9d, 0xa5, 0xef, 0xca, 0x14, 0xbd, 0x48, 0xc8, 0x4c, 0x3b, 0x44, 0x81, 0x43,
	0x0e, 0x28, 0x87, 0x22, 0x7e, 0x40, 0xa9, 0x32, 0x28, 0xaa, 0x3a, 0xe4, 0x45, 0x80, 0xb8, 0xb9,
	0xad, 0x4b, 0x0d, 0x69, 0x8c, 0x12, 0x93, 0x8d, 0xbf, 0xcb, 0x2f, 0x41, 0x76, 0xda, 0xd2, 0xdb,
	0xf3, 0xe5, 0x27, 0xaf, 0x1e, 0x05, 0xe0, 0x41, 0xb7, 0xaa, 0xf8, 0xd5, 0x1a, 0x6b, 0xb2, 0x97,
	0xc0, 0x16, 0xe6, 0xbb, 0x6e, 0xf0, 0x1a, 0xe2, 0xdf, 0x9d, 0x6a, 0x1b, 0xb9, 0x57, 0x3c, 0x48,
	0x83, 0x7c, 0x24, 0x4e, 0x3c, 0x7b, 0x0b, 0x17, 0x33, 0xd3, 0x58, 0xb9, 0xb6, 0x88, 0x40, 0xcf,
	0x22, 0x1e, 0xe3, 0x73, 0x88, 0x4c, 0x53, 0xeb, 0x46, 0x71, 0x92, 0x06, 0x79, 0x2c, 0x0e, 0x2c,
	0xbb, 0x06, 0x5a, 0xa9, 0x47, 0xff, 0x66, 0x65, 0x36, 0x7f, 0x8e, 0x6f, 0x1c, 0xce, 0x5e, 0x03,
	0xbd, 0xd5, 0xb5, 0xc2, 0x04, 0xc2, 0x9f, 0xea, 0x68, 0x39, 0xe8, 0xd2, 0x1b, 0x69, 0xa5, 0xef,
	0x1a, 0x0b, 0x8f, 0x33, 0x0e, 0x64, 0xda, 0x9f, 0x9c, 0xe0, 0xcc, 0xf9, 0x4b, 0x80, 0x7d, 0x90,
	0x2b, 0xd5, 0x22, 0x87, 0x8b, 0x5e, 0xb5, 0x9d, 0x36, 0x8d, 0x0f, 0x3c, 0x15, 0x47, 0x8a, 0x37,
	0x30, 0xea, 0x54, 0xe7, 0xe0, 0x7c, 0xe3, 0x6b, 0x47, 0xe2, 0xbf, 0xe0, 0x5a, 0xb7, 0xad, 0xd9,
	0xf3, 0x70, 0xb8, 0xce, 0x61, 0xbc, 0x02, 0x62, 0x0d, 0xa7, 0x5e, 0x21, 0xd6, 0x60, 0x06, 0xec,
	0x61, 0xa7, 0xd7, 0x3b, 0xce, 0xd2, 0x20, 0xbf, 0x9a, 0x8c, 0x0b, 0xff, 0xc9, 0xe2, 0x8b, 0xd3,
	0xc4, 0x60, 0xe1, 0x0d, 0xb0, 0xda, 0x2d, 0xc9, 0x55, 0x1a, 0xe4, 0x97, 0x93, 0xa8, 0xf0, 0xbb,
	0x8a, 0x41, 0xc4, 0x57, 0x10, 0xaf, 0x87, 0x09, 0x3b, 0xbe, 0x4d, 0xc3, 0xfc, 0x72, 0x12, 0x17,
	0x87, 0x4d, 0xc5, 0xc9, 0xc1, 0x17, 0x40, 0xad, 0x7a, 0xb4, 0x7c, 0xe7, 0x2b, 0x58, 0xe1, 0xe6,
	0x13, 0x5e, 0xc2, 0x67, 0x40, 0x64, 0xcf, 0xb5, 0x37, 0xc2, 0x62, 0xda, 0x0b, 0x22, 0x7b, 0x97,
	0xdf, 0xea, 0x5a, 0xf1, 0x1f, 0x87, 0xbc, 0x9b, 0x54, 0x78, 0x29, 0xfb, 0x08, 0xcc, 0x9f, 0x87,
	0x23, 0x60, 0x8b, 0xbb, 0xf7, 0xf3, 0x65, 0xf2, 0x04, 0xc7, 0x10, 0xcf, 0xee, 0x96, 0xd5, 0x74,
	0x56, 0xdd, 0x27, 0x81, 0x63, 0x9f, 0x44, 0x79, 0x5f, 0x2e, 0x67, 0x65, 0x42, 0x30, 0x06, 0x5a,
	0x95, 0x5f, 0xab, 0x24, 0x74, 0xe8, 0x76, 0xbe, 0x28, 0x13, 0x8a, 0x11, 0x90, 0xe9, 0xe7, 0x84,
	0xbd, 0x8b, 0xbe, 0xd1, 0xbd, 0xd4, 0xcd, 0x2a, 0xf2, 0xff, 0xcc, 0x9b, 0x7f, 0x01, 0x00, 0x00,
	0xff, 0xff, 0x21, 0x73, 0xdd, 0x33, 0x41, 0x02, 0x00, 0x00,
}
